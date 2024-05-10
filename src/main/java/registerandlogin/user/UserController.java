package registerandlogin.user;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.param.PaymentIntentCreateParams;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;


@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class UserController {
	
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    Userrep userRep;
    
    @Autowired
    PasswordEncoder passwordEncoder;
    
    @Autowired
    private JavaMailSender javaMailSender;
    
    @Value("${stripe.secretKey}")
    private String stripeSecretKey;
    
//    awseb-e-brae2nudgc-stack-awsebrdsdatabase-x2n1tnhjncfr.cwcbtqzwwnhh.ap-south-1.rds.amazonaws.com
    
    
    
    
    
    
    
    
    
   @CrossOrigin()
    @PostMapping("/users/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody User newUser) {
        User existingUser = userRepository.findByEmail(newUser.getEmail());
        
        if (existingUser != null && existingUser.getUsername().equals(newUser.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with this email and username already exists!");
        } else {
            String encodedPassword = passwordEncoder.encode(newUser.getPassword());
            newUser.setPassword(encodedPassword);
            User registeredUser = userRepository.save(newUser);
            
            if (registeredUser != null) {
                Long userId = registeredUser.getId();
                return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully! ID: " + userId);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to register user");
            }
        }
    }
   
   
    
  
    @CrossOrigin()
    @PostMapping("/users/login")
    public ResponseEntity<Map<String, String>> loginUser(@Valid @RequestBody User userlog) {
        User users = userRepository.findByEmail(userlog.getEmail());
        Map<String, String> response = new HashMap<>();
//        for (User user : users) {
            boolean encodedPassword1 = passwordEncoder.matches(userlog.getPassword(), users.getPassword());
            System.out.println(userlog.getPassword());
            System.out.println(users.getPayamount());
            System.out.println(users.getPassword());
            System.out.println(encodedPassword1);
            if (users.getEmail().equals(userlog.getEmail()) && encodedPassword1 && users.getPayamount().equals("1000") ) {
//                User user1 = userRepository.findByEmail(users.getEmail());
//                System.out.println(user1);
                users.setLoggedIn(true);
                userRepository.save(users);
                String token = getJWTToken(userlog.getPassword());
                System.out.println(token);
                String userIdStr = String.valueOf(users.getId());
                response.put("token", token);
                response.put("username", users.getUsername());
                response.put("email", users.getEmail());
                response.put("userid", userIdStr);
                response.put("message", "success");
                              
                return ResponseEntity.ok(response);
//            }
        }
        response.put("error", "Incorrect ID and password");
        return ResponseEntity.ok(response);
    }

    

    @CrossOrigin()
    @PostMapping("/users/contact")
    public String ContactUser(@Valid @RequestBody Contactuser contact) {
 
    	Contactuser contactUser = userRepository.save(contact);
            
            if (contactUser != null) {
               
                return "User registered successfully!";
                
            } 
            
            else 
            	
            {
                return "Failed to register user";
            }
        }


    
    @CrossOrigin()
    @PostMapping("/users/enquiry")
    public String EnquiryUser(@Valid @RequestBody Enquiryuser enquiry) {
 
    	Enquiryuser enquiryUser = userRepository.save(enquiry);
            
            if (enquiryUser != null) {
               
                return "User registered successfully!";
            } 
            
            else 
            	
            {
                return "Failed to register user";
            }
        }

    
   
 
    @PostMapping("/freetrialsignup")
    public ResponseEntity<String> signup(@Valid @RequestBody Freetrial signupRequest) {
        // Check if the email already exists
        Freetrial existingUser1 = userRep.findByEmail(signupRequest.getEmail());
        if (existingUser1 != null) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        Freetrial user5 = new Freetrial();
        user5.setUsername(signupRequest.getUsername());
        user5.setEmail(signupRequest.getEmail());
        user5.setPassword(signupRequest.getPassword());
        user5.setCreationDate(LocalDateTime.now());
     
        // Save the user
        userRep.save(user5);

        // Return a success response
        return ResponseEntity.ok("Signup successful");
    }

 
    @Scheduled(cron = "0 0 0 * * *")
    public void closeExpiredAccounts() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Freetrial> usersToClose = userRep.findByCreationDateBeforeAndAccountClosedFalse(sevenDaysAgo);
        for (Freetrial user5 : usersToClose) {
       
            user5.setAccountClosed(true);
            userRep.save(user5);
        }
        userRep.deleteByAccountClosedTrue();
    }
    
    
    

	private String getJWTToken(String password) {
	String secretKey = "mySecretKey";
	List<GrantedAuthority> grantedAuthorities = AuthorityUtils
			.commaSeparatedStringToAuthorityList("ROLE_USER");
	
	String token = Jwts
			.builder()
			.setId("softtekJWT")
			.setSubject(password)
			.claim("authorities",
					grantedAuthorities.stream()
							.map(GrantedAuthority::getAuthority)
							.collect(Collectors.toList()))
			.setIssuedAt(new Date(System.currentTimeMillis()))
			.setExpiration(new Date(System.currentTimeMillis() + 600000))
			.signWith(SignatureAlgorithm.HS512,
					secretKey.getBytes()).compact();

	return token;
}

         
    
  
    
    @CrossOrigin()
    @PostMapping("/users/logout")
    public Status logUserOut(@Valid @RequestBody User user) {
//        List<User> users = userRepository.findAll();
//
//        for (User other : users) {
//            if (other.equals(user)) {
//            	  user.setLoggedIn(false);
//                return Status.SUCCESS;
//            }
//        }
//        return Status.FAILURE;
    	User user2 = userRepository.findByEmail(user.getEmail());
    	System.out.println(user2);
    	System.out.println(user);
    	user2.setLoggedIn(false);
    	userRepository.save(user2);
    	return Status.SUCCESS;
    }

    
    
    @CrossOrigin()
    @PutMapping("/users/payed")
    public Status payed(@Valid @RequestBody User value) {
        List<User> users = userRepository.findAll();
        String email = value.getEmail();
        String newPrice = value.getPayamount();

        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                user.setPayamount(newPrice);
                user.setNewPriceSetTime(LocalDateTime.now()); // Set the timestamp for the new price
                userRepository.save(user);
                return Status.SUCCESS;
            }
        }

        return Status.FAILURE;
    }
    

    // Scheduled task to delete old prices after 1 year
    @Scheduled(fixedRate = 86400000) // Run every 24 hours (24 hours * 60 minutes * 60 seconds * 1000 milliseconds)
    public void deleteOldPrices() {
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);

        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getNewPriceSetTime() != null && user.getNewPriceSetTime().isBefore(oneYearAgo)) {
                // Reset the payamount to its original value and clear the newPriceSetTime
                user.setPayamount(user.getDefaultPayamount()); // Replace this with the original payamount value from your data source
                user.setNewPriceSetTime(null);
                userRepository.save(user);
            }
        }
    }
   


    
    private static final String MAILCHIMP_API_KEY = "398b930066d467639d1b903d62f8f1b9-us21";
    private static final String MAILCHIMP_LIST_ID = "ead7c5b30a";
    
    @CrossOrigin()
    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(@RequestBody String emailed) {
 
        // Validate the email address format
//        if (!isValidEmail(email)) {
//            return ResponseEntity.badRequest().body("Invalid email address format");
//        }

        String mailchimpApiUrl = String.format("https://us21.api.mailchimp.com/3.0/lists/%s/members", MAILCHIMP_LIST_ID);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "apikey " + MAILCHIMP_API_KEY);

        Map<String, Object> request = new HashMap<>();
        request.put("email_address", emailed);
        request.put("status", "subscribed");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(mailchimpApiUrl, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok("Subscription successful!");
            } else {
                return ResponseEntity.status(response.getStatusCode()).body("Subscription failed. Please try again.");
            }
        } catch (HttpClientErrorException.BadRequest e) {
            // Handle Mailchimp API validation errors
            return ResponseEntity.badRequest().body("Mailchimp API error: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            // Handle other exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }
    
    
//    private boolean isValidEmail(String email) {
//        
//        return email != null && email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
//    }
    

    
    
    
    
    
    
    
    @CrossOrigin()
    @PostMapping("/users/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody User email) throws MessagingException {
    	
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
        if (user.getEmail().equals(email.getEmail())) {
           
        String otp = generateOTP();
        
        Date otpExpiry = getExpiryTime();

        user.setOtp(otp);
        
        user.setOtpExpiry(otpExpiry);
        
        userRepository.save(user);

        sendOTPEmail(user.getEmail(), otp);

        Map<String, String> response = new HashMap<>();
        response.put("message", "OTP sent to your email address");

        return ResponseEntity.ok(response);
        
           }
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "OTP not to your email address");
        return ResponseEntity.ok(response);
    }
    

    
    @CrossOrigin()
    @PostMapping("/users/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOTP(@Valid @RequestBody User vv) {
        List<User> users = userRepository.findAll();
        Map<String, String> response = new HashMap<>();
        for (User user : users) {
            

            if (user.getOtpExpiry().before(new Date(5))) {          	
                response.put("message", "OTP has expired");
                return ResponseEntity.ok(response);
            }

            // Add the required row below
            if (user.getOtp().equals(vv.getOtp())) {
                String encodedPassword = passwordEncoder.encode(vv.getPassword());
                user.setPassword(encodedPassword);
                userRepository.save(user);
                response.put("message", "Password updated successfully");
                return ResponseEntity.ok(response);
      
            }
                 
        }

        response.put("message", "user not found");
        return ResponseEntity.ok(response);
    }

    
    private String generateOTP() {
        String digits = "0123456789";
        Random random = new Random();
        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            otp.append(digits.charAt(random.nextInt(digits.length())));
        }

        return otp.toString();
    }

    
    private Date getExpiryTime() {
        long currentTimeMillis = System.currentTimeMillis();
        long otpExpiryMillis = currentTimeMillis + (5 * 60 * 1000); // OTP expires in 5 minutes
        return new Date(otpExpiryMillis);
    }

    
    public void sendOTPEmail(String recipientEmail, String otp) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("jabezraja.riyadvi@gmail.com");
            helper.setTo(recipientEmail);
            helper.setSubject("OTP for Authentication");
            helper.setText("Your OTP is: " + otp);

            javaMailSender.send(message);
        } catch (Exception e) {
            // Handle email sending failure
        }
    }
    
    
    @CrossOrigin()
    @DeleteMapping("/users/all")
    public Status deleteUsers() {
        userRepository.deleteAll();
        return Status.SUCCESS;
    }
    
    
    @CrossOrigin()
    @PostMapping("/create-payment-intent")
    public PaymentResponse createPaymentIntent(@RequestBody Map<String, String> request) {
        Stripe.apiKey = stripeSecretKey;

        // Get the token from the request
        String token = request.get("token");
        String payemail = request.get("email");
        System.out.print(payemail);
        System.out.print(token);
        
        try {
            // Create a PaymentMethod using the token
            Map<String, Object> paymentMethodParams = new HashMap<>();
            paymentMethodParams.put("type", "card");
            paymentMethodParams.put("card[token]", token);

            PaymentMethod paymentMethod = PaymentMethod.create(paymentMethodParams);

            // Create a PaymentIntent using the PaymentMethod ID
            PaymentIntentCreateParams createParams = new PaymentIntentCreateParams.Builder()
                    .setAmount((long) 1000) // Replace with the actual amount in cents (e.g., $10.00)
                    .setCurrency("usd")
                    .setPaymentMethod(paymentMethod.getId())
                    .setConfirm(true)
                    .build();
            System.out.println("ghfhfhd");
            PaymentIntent paymentIntent = PaymentIntent.create(createParams);

            // If paymentIntent.getStatus() is "succeeded", the payment was successful
            boolean paymentSuccess = "succeeded".equals(paymentIntent.getStatus());
            if (paymentSuccess) {
                // Handle the success response
                PaymentResponse response = new PaymentResponse();
                response.setSuccess(true);

                User users = userRepository.findByEmail(payemail);
             
                System.out.print(users);
//                    if (users.getEmail().equalsIgnoreCase(payemail)) {
                    	users.setPayamount("1000");
//                        user.setNewPriceSetTime(LocalDateTime.now()); // Set the timestamp for the new price
                        userRepository.save(users);
                      
                    

                response.setClientSecret(paymentIntent.getClientSecret());
                System.out.println(response);
                return response;
                
                
            } else {
               
                PaymentResponse response = new PaymentResponse();
                response.setSuccess(false);
                response.setErrorMessage("Payment failed. Please try again.");
                return response;
            }
        } catch (StripeException e) {
            // Handle any Stripe-related errors
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(false);
            response.setErrorMessage("Payment processing error: " + e.getMessage());
            return response;
        }
    }
}