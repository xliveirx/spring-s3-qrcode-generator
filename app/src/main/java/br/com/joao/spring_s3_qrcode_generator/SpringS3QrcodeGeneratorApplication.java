package br.com.joao.spring_s3_qrcode_generator;

import br.com.joao.spring_s3_qrcode_generator.controller.UserController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(UserController.class)
public class SpringS3QrcodeGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringS3QrcodeGeneratorApplication.class, args);
	}

}
