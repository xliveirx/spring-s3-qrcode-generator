package br.com.joao.spring_s3_qrcode_generator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class SpringS3QrcodeGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringS3QrcodeGeneratorApplication.class, args);
	}

}
