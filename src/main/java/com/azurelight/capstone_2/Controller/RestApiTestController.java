package com.azurelight.capstone_2.Controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.azurelight.capstone_2.Service.ClassificationService;


@RestController
@CrossOrigin
@RequestMapping("/Rest")
public class RestApiTestController {
	private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
	@GetMapping("/getHelloworld")
	public String moveSignupPage() {
		return "hello world";
	}

	@RequestMapping(value = "/PredictionRequest", method = { RequestMethod.GET, RequestMethod.POST })
	public String PredictionRequest(@RequestParam(value = "image") MultipartFile pins) {
		final String ext = pins.getContentType().split("/")[1];
		final String uuidPinName = UUID.randomUUID() + "." + ext;
		File dest = new File("/home/ubuntu/Capstone2AwsRest/src/main/resources/predictedImages/" + uuidPinName);

		try {
			BufferedInputStream bis = new BufferedInputStream(pins.getInputStream());
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dest));
			int bytesRead = 0;
			byte[] buffer = new byte[1024];
			while ((bytesRead = bis.read(buffer, 0, 1024)) != -1)
				bos.write(buffer, 0, bytesRead);
			bos.close();
			bis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 요청에서 날아온 이미지 저장
		// 경로를 서비스에 전달
		// 결과값 클라이언트에 응답
		ClassificationService cs = new ClassificationService();
		String rr = cs.doClassification("/home/ubuntu/Capstone2AwsRest/src/main/resources/predictedImages/" + uuidPinName);
		log.error("============================================");
		log.error(rr);
		log.error("============================================");
		return rr;
		// return ClassificationService.doClassification("./" + uuidPinName);
	}
}
