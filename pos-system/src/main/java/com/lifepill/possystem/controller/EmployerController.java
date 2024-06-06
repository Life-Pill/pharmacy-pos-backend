package com.lifepill.possystem.controller;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.lifepill.possystem.dto.*;
import com.lifepill.possystem.dto.requestDTO.EmployerUpdate.*;
import com.lifepill.possystem.entity.Branch;
import com.lifepill.possystem.entity.Employer;
import com.lifepill.possystem.exception.NotFoundException;
import com.lifepill.possystem.repo.branchRepository.BranchRepository;
import com.lifepill.possystem.repo.employerRepository.EmployerRepository;
import com.lifepill.possystem.service.EmployerService;
import com.lifepill.possystem.service.S3Service;
import com.lifepill.possystem.util.StandardResponse;
import com.lifepill.possystem.util.mappers.EmployerMapper;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Controller class for managing employer-related operations.
 */
@RestController
@RequestMapping("lifepill/v1/employers")
@AllArgsConstructor
@Log4j2
public class EmployerController {

    private final S3Service s3Service;
    private final EmployerRepository employerRepository;
    private final ModelMapper modelMapper;

    private EmployerService employerService;
    private EmployerMapper employerMapper;
    private BranchRepository branchRepository;

 //   public static String uploadDirectory = System.getProperty("user.dir") + "/uploads";


    @PostMapping("/save-s3")
    public ResponseEntity<EmployerS3DTO> createEmployer(
            @RequestParam("file") MultipartFile file,
            @RequestParam("branchId") Long branchId,
            @ModelAttribute Employer employer
    ) throws IOException {
        Optional<Branch> branchOptional = branchRepository.findById(branchId);
        if (branchOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Branch branch = branchOptional.get();
        employer.setBranch(branch);

        String imageUrl = s3Service.uploadFile(employer.getEmployerEmail(), file);
        employer.setProfileImageUrl(imageUrl);
        Employer savedEmployer = employerRepository.save(employer);

        EmployerS3DTO employerDTO = modelMapper.map(savedEmployer, EmployerS3DTO.class);

        return new ResponseEntity<>(employerDTO, HttpStatus.CREATED);
    }

    @GetMapping(value = "/{id}/image", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<InputStreamResource> getEmployerImage(@PathVariable Long id) {
        Employer employer = employerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employer not found"));

        String imageUrl = employer.getProfileImageUrl();
        log.info("Image URL to retrieve: " + imageUrl);

        // Extract the key from the imageUrl
        String keyName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        S3Object s3Object = s3Service.getFile(keyName);
        S3ObjectInputStream objectInputStream = s3Object.getObjectContent();
        InputStreamResource inputStreamResource = new InputStreamResource(objectInputStream);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + keyName + "\"")
                .body(inputStreamResource);
    }

    /**
     * Saves an employer without an image.
     *
     * @param cashierWithoutImageDTO DTO containing details of the employer without image.
     * @return A string indicating the success of the operation.
     */
    @PostMapping("/save-without-image")
    public ResponseEntity<StandardResponse> saveCashierWithoutImage(@RequestBody EmployerWithoutImageDTO cashierWithoutImageDTO) {
        employerService.saveEmployerWithoutImage(cashierWithoutImageDTO);

        System.out.println(cashierWithoutImageDTO.getEmployerId());
        return new ResponseEntity<>(
                new StandardResponse(201, "successfully saved", cashierWithoutImageDTO),
                HttpStatus.CREATED
        );
    }

    /**
     * Saves an employer with an image.
     *
     * @param employerDTO DTO containing details of the employer including image.
     * @param file        MultipartFile representing the image file.
     * @return A string indicating the success of the operation.
     * @throws IOException If an I/O error occurs.
     */
    @PostMapping("/save-with-image")
    public ResponseEntity<StandardResponse> saveEmployerWithImage(
            @ModelAttribute EmployerDTO employerDTO,
            @RequestParam("file") MultipartFile file
    )
            throws IOException {
        // Check if a file is provided
        if (!file.isEmpty()) {
            // Convert MultipartFile to byte array
            byte[] profileImage = file.getBytes();
            // Set the profile image in the employerDTO
            employerDTO.setProfileImage(profileImage);
        }
        // Save the cashier along with the profile photo
        employerService.saveEmployer(employerDTO);
        return new ResponseEntity<>(
                new StandardResponse(201, "successfully saved", employerDTO),
                HttpStatus.CREATED
        );
    }

    /**
     * Retrieves the profile photo of an employer by ID.
     *
     * @param employerId The ID of the employer.
     * @return ResponseEntity containing the profile photo byte array.
     */
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

    /**
     * Updates details of an employer.
     *
     * @param employerId                 The ID of the employer to be updated.
     * @param cashierAllDetailsUpdateDTO DTO containing updated details of the employer.
     * @return A string indicating the success of the operation.
     */
    @PutMapping("/update/{employerId}")
    @Transactional
    public ResponseEntity<StandardResponse> updateEmployer(
            @PathVariable Long employerId,
            @RequestBody EmployerAllDetailsUpdateDTO cashierAllDetailsUpdateDTO
    ) {
        String message = employerService.updateEmployer(employerId, cashierAllDetailsUpdateDTO);
        return new ResponseEntity<>(
                new StandardResponse(201, message, null),
                HttpStatus.OK
        );
    }

    /**
     * Updates the account details of an employer.
     *
     * @param cashierUpdateAccountDetailsDTO DTO containing updated account details of the employer.
     * @return A string indicating the success of the operation.
     */
    @PutMapping("/updateAccountDetails")
    @Transactional
    public ResponseEntity<StandardResponse> updateEmployerAccountDetails(
            @RequestBody EmployerUpdateAccountDetailsDTO cashierUpdateAccountDetailsDTO
    ) {
        String message = employerService.updateEmployerAccountDetails(cashierUpdateAccountDetailsDTO);
        return new ResponseEntity<>(
                new StandardResponse(201, message, null),
                HttpStatus.OK
        );
    }

    /**
     * Updates the bank account details of an employer.
     *
     * @param employerId                   The ID of the employer to update.
     * @param employerUpdateBankAccountDTO The DTO containing the updated bank account details.
     * @return ResponseEntity containing the updated employer data
     * along with bank account details,
     * or an HTTP status indicating the failure if the employer is not found.
     */
    @PutMapping("/updateEmployerBankAccountDetailsWithId/{employerId}")
    @Transactional
    public ResponseEntity<StandardResponse> updateEmployerBankAccountDetailsWithId(
            @PathVariable long employerId,
            @RequestBody EmployerUpdateBankAccountDTO employerUpdateBankAccountDTO
    ) {
        try {
            EmployerWithBankDTO employerWithBankDTO = employerService
                    .updateEmployerBankAccountDetails(employerUpdateBankAccountDTO);
            EmployerBankDetailsDTO bankDetailsDTO = employerService
                    .getEmployerBankDetailsById(employerId);
            employerWithBankDTO.setEmployerBankDetails(
                    employerMapper.mapBankDetailsDTOToEntity(bankDetailsDTO)
            ); // Utilize the mapper// Map DTO to Entity
            return ResponseEntity.ok(
                    new StandardResponse(
                            201, "SUCCESS", employerWithBankDTO)
            );
        } catch (NotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(
                            new StandardResponse(404, ex.getMessage(), null)
                    );
        }
    }

    /**
     * Retrieves an employer by ID.
     *
     * @param employerId The ID of the employer to retrieve.
     * @return The EmployerDTO object representing the employer.
     */
    @GetMapping(path = "/get-by-id", params = "employerId")
    @Transactional
    public ResponseEntity<StandardResponse> getEmployerById(@RequestParam(value = "employerId") int employerId) {
        EmployerDTO employerDTO = employerService.getEmployerById(employerId);
        return new ResponseEntity<>(
                new StandardResponse(201, "SUCCESS", employerDTO),
                HttpStatus.OK
        );
    }

    /**
     * Retrieves the image of an employer by ID.
     *
     * @param employerId The ID of the employer whose image is to be retrieved.
     * @return ResponseEntity containing the image byte array.
     */
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

    /**
     * Deletes an employer by ID.
     *
     * @param employerId The ID of the employer to delete.
     * @return A string indicating the success of the operation.
     */
    @DeleteMapping(path = "/delete-employerId/{id}")
    public ResponseEntity<StandardResponse> deleteEmployer(@PathVariable(value = "id") int employerId) {
        String deleted = employerService.deleteEmployer(employerId);
        return new ResponseEntity<>(
                new StandardResponse(201, deleted, null),
                HttpStatus.OK
        );
    }

    /**
     * Retrieves all employers.
     *
     * @return ResponseEntity containing a list of all employers.
     */
    @GetMapping(path = "/get-all-employers")
    public ResponseEntity<StandardResponse> getAllEmployers() {
        List<EmployerDTO> allEmployer = employerService.getAllEmployer();
        return new ResponseEntity<>(
                new StandardResponse(201, "SUCCESS", allEmployer),
                HttpStatus.OK
        );
    }


    /**
     * Retrieves all employers.
     *
     * @return ResponseEntity containing a list of all employers.
     */
    @GetMapping(path = "/get-all-employers-by-active-state/{status}")
    @Transactional
    public List<EmployerDTO> getAllEmployerByActiveState(@PathVariable(value = "status") boolean activeState) {
        return employerService.getAllEmployerByActiveState(activeState);
    }

    /**
     * Retrieves bank details of all employers.
     *
     * @return ResponseEntity containing a StandardResponse object with a list of employer bank details.
     */
    @GetMapping(path = "/get-all-employers-bank-details")
    @Transactional
    public ResponseEntity<StandardResponse> getAllEmployerBankDetails() {
        List<EmployerUpdateBankAccountDTO> allCashiersBankDetails = employerService.getAllEmployerBankDetails();
        return new ResponseEntity<>(
                new StandardResponse(201, "SUCCESS", allCashiersBankDetails),
                HttpStatus.OK
        );
    }

    /**
     * Retrieves bank details of an employer by ID.
     *
     * @param employerId The ID of the employer.
     * @return ResponseEntity containing the bank details of the employer,
     * or an HTTP status indicating the failure if the employer is not found.
     */
    @GetMapping("/bank-details/{employerId}")
    public ResponseEntity<StandardResponse> getEmployerBankDetailsById(@PathVariable long employerId) {
        try {
            // Retrieve bank details of the employer by ID
            EmployerBankDetailsDTO bankDetailsDTO = employerService.getEmployerBankDetailsById(employerId);

            if (bankDetailsDTO != null) {
                // Return the bank details with a success response
                return ResponseEntity.ok(
                        new StandardResponse(200, "SUCCESS", bankDetailsDTO)
                );
            } else {
                // If the bank details are not found, return a not found response
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new StandardResponse(404, "Bank details not found for employer ID: " + employerId, null));
            }
        } catch (NotFoundException ex) {
            // If an exception occurs (e.g., employer not found), return a not found response
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new StandardResponse(404, ex.getMessage(), null));
        }
    }

    /**
     * Retrieves all employers with bank details.
     *
     * @return ResponseEntity containing a list of EmployerWithBankDTO objects,
     * or an HTTP status indicating the failure if no employers are found.
     */
    @GetMapping("/employers-with-bank-details")
    public ResponseEntity<StandardResponse> getAllEmployersWithBankDetails() {
        List<EmployerWithBankDTO> allEmployersWithBankDetails = employerService.getAllEmployersWithBankDetails();
        if (allEmployersWithBankDetails != null && !allEmployersWithBankDetails.isEmpty()) {
            return ResponseEntity.ok(
                    new StandardResponse(200, "SUCCESS", allEmployersWithBankDetails)
            );
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new StandardResponse(404, "No employers found", null));
        }
    }

    /**
     * Retrieves an employer along with bank details by ID.
     *
     * @param employerId The ID of the employer.
     * @return ResponseEntity containing the employer data along with bank details,
     * or an HTTP status indicating the failure if the employer is not found.
     */
    @GetMapping("/with-bank-details/{employerId}")
    public EmployerWithBankDTO getEmployerWithBankDetailsById(@PathVariable long employerId) {
        return employerService.getEmployerWithBankDetailsById(employerId);
    }
}