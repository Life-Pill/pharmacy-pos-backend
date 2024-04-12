package com.lifepill.possystem.controller;

import com.lifepill.possystem.dto.EmployerDTO;
import com.lifepill.possystem.dto.EmployerWithoutImageDTO;
import com.lifepill.possystem.dto.requestDTO.EmployerUpdate.*;
import com.lifepill.possystem.entity.enums.Role;
import com.lifepill.possystem.service.EmployerService;
import com.lifepill.possystem.util.StandardResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Date;
import java.util.List;

@RestController
@RequestMapping("lifepill/v1/employers")
@CrossOrigin
public class EmployerController {

    @Autowired
    private EmployerService employerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public static String uploadDirectory = System.getProperty("user.dir") + "/uploads";

    @PostMapping("/save-without-image")
    public String saveCashierWithoutImage(@RequestBody EmployerWithoutImageDTO cashierWithoutImageDTO) {
//        employerService.saveEmployerWithoutImage(cashierWithoutImageDTO);
//        return "saved";
        try {
            // Encode the password
            String hashedPassword = passwordEncoder.encode(cashierWithoutImageDTO.getEmployerPassword());
            cashierWithoutImageDTO.setEmployerPassword(hashedPassword);

            // Save the employer
            employerService.saveEmployerWithoutImage(cashierWithoutImageDTO);

            // Return success response
            return "Employer registered successfully";
        } catch (Exception ex) {
            // Return error response if an exception occurs
            return "An exception occurred: " + ex.getMessage();
        }
    }



/*    @PostMapping("/save-with-image-local-and send-name-to-database")
    public String saveCashier(@ModelAttribute EmployerDTO cashierDTO, @RequestParam("Profile_image") MultipartFile file) throws IOException{
        String originalFilename = file.getOriginalFilename();
        Path fileNameAndPath = Paths.get(uploadDirectory, originalFilename);
        Files.write(fileNameAndPath, file.getBytes());
        cashierDTO.setProfileImage(originalFilename);
        cashierService.saveCashier(cashierDTO);
        return "saved";
    }*/

/*    @GetMapping(path = "/get-by-id-with-image/{id}")
    public ResponseEntity<EmployerDTO> getCashierByIdWithImage(@PathVariable(value = "id") int cashierId) {
        EmployerDTO cashierDTO = cashierService.getCashierByIdWithImage(cashierId);
        return new ResponseEntity<EmployerDTO>(cashierDTO, HttpStatus.OK);

    }*/

    @PostMapping("/save-with-image")
    public String saveEmployerWithImage(@ModelAttribute EmployerDTO employerDTO, @RequestParam("file") MultipartFile file) throws IOException {
        // Check if a file is provided
        if (!file.isEmpty()) {
            // Convert MultipartFile to byte array
            byte[] profileImage = file.getBytes();
            // Set the profile image in the employerDTO
            employerDTO.setProfileImage(profileImage);
        }
        // Save the cashier along with the profile photo
        employerService.saveEmployer(employerDTO);
        return "saved";
    }

    @GetMapping("/profile-photo/{employerId}")
    @Transactional
    public ResponseEntity<byte[]> getProfilePhoto(@PathVariable int employerId) {
        // Retrieve the cashier entity by ID
        EmployerDTO cashier = employerService.getEmployerById(employerId);

        // Check if the cashier exists
        if (cashier != null && cashier.getProfileImage() != null) {
            // Return the profile image byte array with appropriate headers
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.IMAGE_JPEG) // Adjust content type based on image format
                    .body(cashier.getProfileImage());
        } else {
            // If the cashier or profile photo doesn't exist, return a not found response
            return ResponseEntity.notFound().build();
        }
    }


   /* @PutMapping("/update")
    @Transactional
    public String updateCashier(@RequestBody EmployerUpdateDTO cashierUpdateDTO) {
        String message = cashierService.updateCashier(cashierUpdateDTO);
        return message;
    }*/

    // need to add both place cashier ID
   @PutMapping("/update/{employerId}")
   @Transactional
   public String updateEmployer(@PathVariable Long employerId, @RequestBody EmployerAllDetailsUpdateDTO cashierAllDetailsUpdateDTO) {
       String message = employerService.updateEmployer(employerId, cashierAllDetailsUpdateDTO);
       return message;
   }


    @PutMapping("/updateAccountDetails")
    @Transactional
    public String updateEmployerAccountDetails(@RequestBody EmployerUpdateAccountDetailsDTO cashierUpdateAccountDetailsDTO) {
        String message = employerService.updateEmployerAccountDetails(cashierUpdateAccountDetailsDTO);
        return message;
    }

/*    @PutMapping("/updateBankAccountDetails")
    @Transactional
    public String updateCashierBankAccountDetails(@RequestBody EmployerUpdateBankAccountDTO cashierUpdateBankAccountDTO) {
        String message = cashierService.updateCashierBankAccountDetails(cashierUpdateBankAccountDTO);
        return message;
    }*/

    @PutMapping("/updateBankAccountDetails/{employerId}")
    @Transactional
    public String updateEmployerBankAccountDetails(@PathVariable long employerId, @RequestBody EmployerUpdateBankAccountDTO cashierUpdateBankAccountDTO) {
        String message = employerService.updateEmployerBankAccountDetailsByCashierId(employerId, cashierUpdateBankAccountDTO);
        return message;
    }


    @PutMapping("/updatePassword")
    @Transactional
    public String updateEmployerPassword(@RequestBody EmployerPasswordResetDTO cashierPasswordResetDTO) {
        String message = employerService.updateEmployerPassword(cashierPasswordResetDTO);
        return message;
    }

    @PutMapping("/updateRecentPin")
    @Transactional
    public String updateRecentPin(@RequestBody EmployerRecentPinUpdateDTO cashierRecentPinUpdateDTO) {
        String message = employerService.updateRecentPin(cashierRecentPinUpdateDTO);
        return message;
    }

    @GetMapping(path = "/get-by-id", params = "id")
    @Transactional
    public EmployerDTO getEmployerById(@RequestParam(value = "id") int employerId) {
        EmployerDTO employerDTO = employerService.getEmployerById(employerId);
        return employerDTO;
    }

    @GetMapping("/view-image/{employerId}")
    @Transactional
    public ResponseEntity<byte[]> viewImage(@PathVariable int employerId) {
        byte[] imageData = employerService.getImageData(employerId);

        if (imageData != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG); // Adjust the media type based on your image format
            return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping(path = "/delete-employerId/{id}")
    public String deleteEmployer(@PathVariable(value = "id") int employerId) {
        String deleted = employerService.deleteEmployer(employerId);
        return deleted;
    }

    @GetMapping(path = "/get-all-employers")
    public ResponseEntity<StandardResponse> getAllEmployers() {
        List<EmployerDTO> allEmployer = employerService.getAllEmployer();
        return new ResponseEntity<StandardResponse>(
                new StandardResponse(201, "SUCCESS", allEmployer),
                HttpStatus.OK
        );
    }

    @GetMapping(path = "/get-all-employers-by-active-state/{status}")
    @Transactional
    public List<EmployerDTO> getAllEmployerByActiveState(@PathVariable(value = "status") boolean activeState) {
        List<EmployerDTO> allemployer = employerService.getAllEmployerByActiveState(activeState);
        return allemployer;
    }

    @GetMapping(path = "/get-all-employers-bank-details")
    @Transactional
    public ResponseEntity<StandardResponse> getAllEmployerBankDetails() {
        List<EmployerUpdateBankAccountDTO> allCashiersBankDetails = employerService.getAllEmployerBankDetails();
        return new ResponseEntity<StandardResponse>(
                new StandardResponse(201, "SUCCESS", allCashiersBankDetails),
                HttpStatus.OK
        );
    }

    @GetMapping("/byRole/{role}")
    public ResponseEntity<List<EmployerDTO>> getAllEmployerByRole(@PathVariable Role role) {
        List<EmployerDTO> cashiers = employerService.getAllEmployerByRole(role);
        return new ResponseEntity<>(cashiers, HttpStatus.OK);
    }
}