package com.azurelight.capstone_2.Controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.Map;
import java.util.Optional;
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

import com.azurelight.capstone_2.Repository.FriendRepository;
import com.azurelight.capstone_2.Repository.UserRepository;
import com.azurelight.capstone_2.Service.ClassificationService;
import com.azurelight.capstone_2.Service.FCMService;
import com.azurelight.capstone_2.Service.UserDataInitializer;
import com.azurelight.capstone_2.Service.Utility;
import com.azurelight.capstone_2.Service.Noti.NotificationRequest;
import com.azurelight.capstone_2.db.Friend;
import com.azurelight.capstone_2.db.User;
import com.google.firebase.messaging.FirebaseMessagingException;

import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// /home/ubuntu/Capstone2AwsRest/src/main/resources/profiles/
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
	private FCMService fs;
	@Autowired
	private UserRepository ur;
	@Autowired
	private PasswordEncoder pe;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private FriendRepository friendRepository;
	@Autowired
	private UserDataInitializer userdataInitializer;

	private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

	@GetMapping("fetch-userdata")
	public Map<String, Object> fetchUserData(@RequestParam(value = "email") String email) {
		// UserDataInitializer userdataInitializer = new UserDataInitializer(email);
		userdataInitializer.setTargetUserEmail(email);
		return Map.of("friendlist", userdataInitializer.userFriendsFetcher(),
				"messagelist", userdataInitializer.userMessagesFetcher(),
				"friendRequestNotificationlist", userdataInitializer.userFriendRequestNotificationFetcher());
	}

	@PostMapping("/signin")
	public Map<String, Object> signin(@RequestParam(value = "email") String email,
			@RequestParam(value = "password") String password, @RequestParam(value = "fcmtoken") String fcmtoken) {

		Optional<User> user = ur.findById(email);

		if (!user.isPresent()) {
			return Map.of("code", 1);
		} else if (!pe.matches(password, user.get().getPassword())) {
			return Map.of("code", 2);
		} else {
			User me = user.get();
			if (!(me.getFcmtoken().equals(fcmtoken)))
				ur.updateFcmbyEmail(me.getEmail(), fcmtoken);
			return Map.of("code", 0);
		}
	}

	@PostMapping("/signup")
	@ResponseBody
	public Map<String, Object> signup(@RequestBody SignupRequestData req) {
		if (!(ur.findById(req.getEmail()).isPresent())) {
			ur.save(new User(req.getEmail(), pe.encode(req.getPassword()), null,
					req.getFcmtoken()));
			return Map.of("requestResult", "signup success", "code", 0);
		} else
			return Map.of("requestResult", "Your email is duplicated.", "code", 1);
	}

	@GetMapping(value = "/get-profile/{email}")
	public byte[] getRequestProfile(@PathVariable("email") String email) {
		System.out.println("in get profile : " + email);
		final User u = ur.findById(email).get();
		final String path = "/home/ubuntu/Capstone2AwsRest/src/main/resources/profiles/" + u.getProfile_image();

		if (u.getProfile_image() == null)
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

	@GetMapping("/get-newuser")
	public Map<String, String> getNewFriendSearchResult(@RequestParam(value = "me") String me,
			@RequestParam(value = "keyword") String keyword) {

		Set<String> allusers = new HashSet<>(userRepository.findAll().stream().map(User::getEmail).toList());
		allusers.remove(me);

		Set<String> friends = new HashSet<>(
				friendRepository.findByUserEmail(me).stream()
						.filter(f -> !(friendRepository.findByTwoUser(f.getFriendemail(), me).isEmpty()))
						.map(Friend::getFriendemail).toList());

		allusers.removeAll(friends);

		Iterator<String> it = allusers.iterator();
		while (it.hasNext()) {
			String n = it.next();
			if (n.contains(keyword)) {
				if (!friendRepository.findByTwoUser(me, n).isEmpty()) {
					return Map.of("result", n, "requestState", "wait");
				} else if (!friendRepository.findByTwoUser(n, me).isEmpty()) {
					return Map.of("result", n, "requestState", "accept");
				} else {
					return Map.of("result", n, "requestState", "init");
				}
			}
		}
		return Map.of("result", "none");
	}

	@PostMapping("/add-friend-acc")
	public Map<String, Object> doAcceptAddFriend(@RequestParam(value = "me") String me,
			@RequestParam(value = "audience") String audience) {

		String fcmtoken = userRepository.findById(audience).get().getFcmtoken();
		friendRepository.save(new Friend(UUID.randomUUID() + "", me, audience, "$none$"));

		try {
			fs.sendNotification(new NotificationRequest(fcmtoken, "친추 수락함", me + " <- 얘가"),
					Map.of("notitype", "friendAddReplyNoti", "fromemail", me));
		} catch (FirebaseMessagingException e) {
			e.printStackTrace();
		}

		return Map.of("result", "success");
	}

	@PostMapping("/add-friend-refuse")
	public Map<String, Object> doRefuseAddFriend(@RequestParam(value = "me") String me,
			@RequestParam(value = "audience") String audience) {
		friendRepository.deleteByTwoUser(audience, me);
		return Map.of("result", "success");
	}

	@PostMapping("/add-friend-req")
	public Map<String, Object> doRequestAddFriend(@RequestParam(value = "me") String me,
			@RequestParam(value = "audience") String audience) {
		String fcmtoken = userRepository.findById(audience).get().getFcmtoken();
		friendRepository.save(new Friend(UUID.randomUUID() + "", me, audience, "$none$"));

		try {
			fs.sendNotification(new NotificationRequest(fcmtoken, "친추옴", me + " <- 얘한테"),
					Map.of("notitype", "friendAddNoti", "fromemail", me, "timestamp",
							Utility.getCurrentDateTimeAsString()));
		} catch (FirebaseMessagingException e) {
			e.printStackTrace();
		}
		return Map.of("result", "success");
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
		ur.updateUserprofile(profileImageName, email);
		return "lilpa 700";
	}

	@RequestMapping(value = "/predict", method = { RequestMethod.GET, RequestMethod.POST })
	public String PredictionRequest(@RequestParam(value = "image") MultipartFile pins) {
		final String ext = pins.getContentType().split("/")[1];
		final String uuidPinName = UUID.randomUUID() + "." + ext;
		File dest = new File("/Users/nicode./Capstone2AwsRest/src/main/resources/predictedImages/" + uuidPinName);

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
				.doClassification("/Users/nicode./Capstone2AwsRest/src/main/resources/predictedImages/" + uuidPinName);
		log.error("============================================");
		log.error(rr);
		log.error("============================================");
		return rr;
	}
}
