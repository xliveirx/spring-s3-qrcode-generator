package br.com.joao.spring_s3_qrcode_generator;

import br.com.joao.spring_s3_qrcode_generator.controller.UserController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@Import(UserController.class)
public class SpringS3QrcodeGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringS3QrcodeGeneratorApplication.class, args);
	}

}
