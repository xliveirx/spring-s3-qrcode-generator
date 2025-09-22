# Spring S3 QRCode Generator

Aplicação Spring Boot que gerencia usuários e gera QR Codes armazenando os arquivos no Amazon S3. A API roda em AWS Lambda (HTTP API) e usa MySQL (RDS). Autenticação via JWT e autorização por perfis (USER/ADMIN).

## Tecnologias
- Java 17, Spring Boot 3 (Web, Security, Data JPA)
- MySQL 8 (RDS) + HikariCP
- AWS: Lambda, API Gateway HTTP API (v2), S3, RDS, VPC, NAT
- Terraform (infra as code)
- ZXing (geração de QR Codes)

## Estrutura de Pastas
```
spring-s3-qrcode-generator/
  app/
    docker-compose.yml
    local/
      run-local.sh
      start-local.sh
    pom.xml
    src/main/java/br/com/joao/spring_s3_qrcode_generator/
      SpringS3QrcodeGeneratorApplication.java
      StreamLambdaHandler.java
      config/
      controller/
      domain/
      dto/
      exception/
      repository/
      service/
    src/main/resources/
      application.properties                (base, sem datasource)
      application-docker.properties         (perfil docker)
      application-lambda.properties         (perfil lambda)
  infra/
    *.tf                                   (VPC, RDS, Lambda, API, S3, IAM)
    env/prod/terraform.tfvars
```

## Perfis e Configurações
- Perfil `docker`: conecta em `mysql:3306` (usado no docker-compose).
- Perfil `lambda`: lê `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` do ambiente.
- `application.properties` não define datasource para evitar conexões locais acidentais.
- No perfil `lambda`, está `spring.jpa.hibernate.ddl-auto=update` apenas para criar o schema inicialmente (recomenda-se migrar para Flyway e voltar para `none`).

## Autenticação e Autorização
- JWT com HMAC (chave exemplo no `TokenService`).
- Públicas: `POST /api/v1/login`, `POST /api/v1/users`.
- ADMIN: `GET /api/v1/users`, `PATCH /api/v1/users/enable/{id}`, `PATCH /api/v1/users/disable/{id}`.
- Demais rotas: autenticadas (ROLE_USER suficiente se não especificado).

## Endpoints
- Autenticação
  - POST `/api/v1/login` → `{ email, password }` → `{ token }`
- Usuários
  - POST `/api/v1/users` → cria usuário
  - GET `/api/v1/users` → lista usuários (ADMIN)
  - DELETE `/api/v1/users` → desativa usuário logado
  - PATCH `/api/v1/users/enable/{id}` → ativa usuário (ADMIN)
  - PATCH `/api/v1/users/disable/{id}` → desativa usuário por id (ADMIN)
- QR Codes
  - POST `/api/v1/qrcodes` → cria QR Code para o usuário logado (salva no S3 e retorna URL assinada)
  - GET `/api/v1/qrcodes` → lista QR Codes do usuário com URL assinada
  - DELETE `/api/v1/qrcodes/{id}` → desativa QR Code; lança 403 se não for o dono

Header para rotas autenticadas: `Authorization: Bearer <token>`

## Executando Localmente (Docker Compose)
1) Build do app
```
cd app
./mvnw clean package -DskipTests
```
2) Subir MySQL e app
```
cd app
docker compose up -d --build
# ou
./local/start-local.sh
```
3) Testar rápido
```
# criar usuário
curl -s -X POST http://localhost:8080/api/v1/users \
  -H 'Content-Type: application/json' \
  -d '{"fullName":"Admin","email":"admin@example.com","password":"123456","confirmPassword":"123456"}'

# login
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@example.com","password":"123456"}' | jq -r .token)

# listar usuários (ROLE_ADMIN requerido)
curl -s http://localhost:8080/api/v1/users -H "Authorization: Bearer $TOKEN"
```
Observações:
- `application-docker.properties` define datasource e `aws.s3.bucket-name`.
- Para usar S3 localmente, configure credenciais AWS e `AWS_REGION` se necessário.

## Deploy com Terraform (AWS)
Pré-requisitos
- Bucket S3 para armazenar o artefato da Lambda (`lambda_bucket_name`).
- Credenciais/Role com permissão para criar VPC, Subnets, NAT, RDS, Lambda, API, S3, IAM.

Passos
```
# 1) Build do artefato
cd app
./mvnw clean package -DskipTests

# 2) Aplicar infra
cd ../infra
terraform init
terraform apply -auto-approve \
  -var="aws_region=<REGION>" \
  -var="bucket_name=<S3_BUCKET_QRCODES>" \
  -var="lambda_bucket_name=<S3_BUCKET_LAMBDA>" \
  -var="db_username=<USER>" \
  -var="db_password=<PASS>" \
  -var="lambda_handler=br.com.joao.spring_s3_qrcode_generator.StreamLambdaHandler::handleRequest"
```
Detalhes relevantes do `lambda.tf`
- Upload do JAR para S3 com `etag = filemd5(...)` para detectar mudanças.
- `source_code_hash = filebase64sha256(...)` para forçar atualização do código da Lambda.
- Variáveis de ambiente da Lambda: `SPRING_PROFILES_ACTIVE=lambda` e `DB_*`.
- Lambda em subnets privadas; RDS em subnets privadas; SG do RDS permite 3306 a partir do SG da Lambda.

## Troubleshooting
- Conexão recusada ao MySQL
  - SG do RDS: inbound 3306 a partir do SG da Lambda.
  - Lambda e RDS na mesma VPC/subnets privadas; rotas/NAT corretas.
  - Confirme que a atualização de código ocorreu (ver `source_code_hash` e `etag`).
- Tabelas inexistentes (ex.: `tb_users`)
  - Com `ddl-auto=update`, o schema é criado na 1ª execução. Recomenda-se migrar para Flyway e alterar para `none`.
- 401/403 JSON padronizado
  - Rotas exigem papéis corretos; envie `Authorization: Bearer <token>`.
- S3
  - `aws.s3.bucket-name` deve existir; a role da Lambda precisa de `s3:GetObject/PutObject/DeleteObject`.

## Exceções e Respostas de Erro
Tratadas por `GlobalExceptionHandler` (JSON `ApiError`):
- 404 `NotFoundException`
- 409 `ConflictException`
- 400 `BadRequestException`
- 403 `ForbiddenException` (ex.: deletar QR de outro usuário)
- 500 `StorageException` (S3)
- 401 `TokenException` (JWT)

Exemplo de erro
```json
{
  "timestamp": "2025-09-22T19:30:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "User is not the owner of this QRCode",
  "path": "/api/v1/qrcodes/123"
}
```

## Roadmap
- Adicionar Flyway; retornar `ddl-auto` para `none`.
- Externalizar segredo do JWT (SSM/Secrets Manager) e rotação de chaves.
- Tests (unitários/integração) e pipeline CI/CD (GitHub Actions) para build + `terraform apply`.

## Licença
Uso educacional/demonstrativo. Ajuste conforme necessário.
