package br.com.joao.spring_s3_qrcode_generator;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamLambdaHandler implements RequestStreamHandler {
    private static SpringBootLambdaContainerHandler<HttpApiV2ProxyRequest, AwsProxyResponse> handler;
    static {
        try {
            // Garante profile lambda por fallback quando rodando na AWS (sem sobrescrever caso já setado)
            if (System.getenv("AWS_LAMBDA_FUNCTION_NAME") != null && System.getProperty("spring.profiles.active") == null
                    && System.getenv("SPRING_PROFILES_ACTIVE") == null) {
                System.setProperty("spring.profiles.active", "lambda");
            }
            handler = SpringBootLambdaContainerHandler.getHttpApiV2ProxyHandler(SpringS3QrcodeGeneratorApplication.class);
        } catch (ContainerInitializationException e) {
            // if we fail here. We re-throw the exception to force another cold start
            e.printStackTrace();
            throw new RuntimeException("Could not initialize Spring Boot application", e);
        }
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        handler.proxyStream(inputStream, outputStream, context);
    }
}