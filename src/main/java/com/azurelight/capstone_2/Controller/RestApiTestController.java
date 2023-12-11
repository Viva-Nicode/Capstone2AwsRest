package com.azurelight.capstone_2.Controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.azurelight.capstone_2.Repository.UserRepository;
import com.azurelight.capstone_2.Service.ClassificationService;
import com.azurelight.capstone_2.Service.FCMService;
import com.azurelight.capstone_2.Service.Noti.NotificationRequest;
import com.azurelight.capstone_2.db.User;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;

import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// request body 확인용 코드
/*
	String bodyJson = "";
	StringBuilder stringBuilder = new StringBuilder();
	BufferedReader br = null;
	String line = "";
	try {
		InputStream inputStream = req.getInputStream();
		if (inputStream != null) {
			br = new BufferedReader(new InputStreamReader(inputStream));
			while ((line = br.readLine()) != null)
				stringBuilder.append(line);
		} else
			log.info("Data 없음");
	} catch (IOException e) {
		e.printStackTrace();
	}
	bodyJson = stringBuilder.toString();
	log.info(bodyJson);
 */

@Getter
@Setter
@AllArgsConstructor
class SignupRequestData {
	private String email;
	private String password;
	private String fcmtoken;

	@Override
	public String toString() {
		return this.email + " " + this.password;
	}
}

@RestController
@RequestMapping("/rest")
public class RestApiTestController {
	@Autowired
	private UserRepository ur;

	@Autowired
	private PasswordEncoder pe;

	@Autowired
	private FCMService fs;

	private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

	@GetMapping("/echo")
	public String moveSignupPage() {
		return "hello world";
	}

	@PostMapping("/signin")
	public Map<String, Object> signin(@RequestBody SignupRequestData req) {
		List<User> l = ur.findByEmail(req.getEmail());

		if (l.isEmpty())
			return Map.of("requestResult", "Non-existent email.", "code", 1);
		else if (!pe.matches(req.getPassword(), l.get(0).getPassword()))
			return Map.of("requestResult", "Password mismatch.", "code", 2);
		else
			return Map.of("requestResult", "sign in success.", "code", 0);
	}

	@PostMapping("/signup")
	@ResponseBody
	public Map<String, Object> signup(@RequestBody SignupRequestData req) {
		if (ur.findByEmail(req.getEmail()).isEmpty()) {
			ur.save(new User(UUID.randomUUID() + "", req.getEmail(), pe.encode(req.getPassword()), null, null, req.getFcmtoken()));
			return Map.of("requestResult", "signup success", "code", 0);
		} else
			return Map.of("requestResult", "Your email is duplicated.", "code", 1);
	}

	@GetMapping(value = "/get-profile/{email}")
	public byte[] getRequestProfile(@PathVariable("email") String email) {
		final User u = ur.findByEmail(email).get(0);
		final String path = "/home/ubuntu/Capstone2AwsRest/src/main/resources/profiles/" + u.getProfile_image_path();
		
		if (u.getProfile_image_path() == null)
			return null;
		
		File file = new File(path);

		byte[] byteImage = null;

		BufferedImage originalImage = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			originalImage = ImageIO.read(file);
			ImageIO.write(originalImage, "jpg", baos);
			baos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (baos != null) {
					baos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		byteImage = baos.toByteArray();
		return byteImage;
	}

	@PostMapping("/store-profile")
	public String uploadProfile(@RequestParam(value = "image") MultipartFile profileImage,
			@RequestParam(value = "email") String email) {
		final String ext = profileImage.getContentType().split("/")[1];
		final String profileImageName = UUID.randomUUID() + "." + ext;
		File dest = new File("/home/ubuntu/Capstone2AwsRest/src/main/resources/profiles/" + profileImageName);

		try {
			BufferedInputStream bis = new BufferedInputStream(profileImage.getInputStream());
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
		int c = ur.updateUserprofile(profileImageName, email);
		log.info("update result code : " + c);

		String result = "ine";
		return result;
	}

	@RequestMapping(value = "/predict", method = { RequestMethod.GET, RequestMethod.POST })
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

		ClassificationService cs = new ClassificationService();
		String rr = cs
				.doClassification("/home/ubuntu/Capstone2AwsRest/src/main/resources/predictedImages/" + uuidPinName);
		log.error("============================================");
		log.error(rr);
		log.error("============================================");
		return rr;
	}

	@PostMapping(value = "/send-noti")
	public String SendNotification(@RequestParam(value = "email") String email){
		final User u = ur.findByEmail(email).get(0);
		try {
			fs.sendNotification(new NotificationRequest(u.getFcmtoken(), email, "test message"));
		} catch (FirebaseMessagingException e) {
			e.printStackTrace();
		}
		return "";
	}
}
