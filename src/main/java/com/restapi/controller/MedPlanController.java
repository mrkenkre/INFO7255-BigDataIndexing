package com.restapi.controller;
import com.restapi.MedPlanApp;
import com.restapi.service.MedPlanService;
import com.restapi.service.OAuthService;
import com.restapi.validation.exceptions.MedPlanNotFoundException;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
public class MedPlanController {
    @Autowired
    private MedPlanService medPlanService;
    @Autowired
    private RabbitTemplate template;
    @Autowired
    private OAuthService oauth;

    public void MedPlanController(MedPlanService medPlanService, RabbitTemplate template) {
        this.medPlanService = medPlanService;
        this.template = template;
    }
    @PostMapping("/medplans")
    public ResponseEntity<String> savePlan(@RequestBody String medPlans, @RequestHeader HttpHeaders headers,@RequestHeader(value="Authorization", required = false) String bearerToken) throws IOException, MedPlanNotFoundException {

        if (!oauth.verifier(bearerToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Invalid Access Token\"}");
        }

        try (InputStream inputStream = getClass().getResourceAsStream("/JSONSchema.json")) {
            JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
            Schema schema = SchemaLoader.load(rawSchema);
            schema.validate(new JSONObject(medPlans));
        }
        JSONObject json = new JSONObject(medPlans);
        String key= (String) json.get("objectId");
        Map<String, Object> plan;
        plan = medPlanService.fetchMedPlanById(key);
        if (plan!=null && !plan.isEmpty()) {

            System.out.println("Plan content:");

            for (Map.Entry<String, Object> entry : plan.entrySet()) {
                String key1 = entry.getKey();
                Object value = entry.getValue();
                System.out.println("Key: " + key1 + ", Value: " + value);
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Plan with the objectId "+key+" already exists!");
        }
        String result = medPlanService.saveMedPlan(key,json);
        if (!result.isEmpty()) {

            Map<String, String> message = new HashMap<String, String>();
            message.put("operation", "SAVE");
            message.put("body", medPlans);

            System.out.println("Sending message: " + message);
            template.convertAndSend(MedPlanApp.queueName, message);

            String etag = generateETag(medPlans);
            medPlanService.saveETag(key,etag);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setETag(etag);
            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body("Plan created Successfully with id: "+key);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    @GetMapping(value = "/medplans", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> fetchAllMedPlans(@RequestHeader(value="Authorization", required = false) String bearerToken) {
        if (!oauth.verifier(bearerToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Invalid Access Token\"}");
        }
        String medPlans = medPlanService.fetchAllMedPlans();
        System.out.println("Here....");

            if (medPlans != null) {
                String etag = generateETag(medPlans);
                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.setETag(etag);
                return ResponseEntity.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(responseHeaders)
                        .body(medPlans);
            } else {
                String errorMessage = "{\"message\":\"No Data Found!\"}";
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorMessage);
            }
       // return null;
    }
    private String generateETag(String medPlans) {
        return  "\"ETag"  + medPlans.hashCode() + "\"";

    }
    @GetMapping("/medplans/{id}")
    public ResponseEntity<?> fetchMedPlanById(@PathVariable("id") String id, @RequestHeader(value="If-None-Match") String im,@RequestHeader(value="Authorization", required = false) String bearerToken) throws MedPlanNotFoundException {

        if (!oauth.verifier(bearerToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Invalid Access Token\"}");
        }

        String etag = medPlanService.fetchETag(id);
        if (etag == null || etag.isEmpty()) {
            String errorMessage = "{\"message\":\"Pass appropriate objectId or ETag!\"}";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorMessage);
        }

        if (im.equals("")) {
            String errorMessage = "{\"message\":\"Pass appropriate ETag!\"}";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorMessage);
        } else if (medPlanService.fetchETag(id).equals(im)) {

            Map<String, Object> medPlans;
        medPlans = medPlanService.fetchMedPlanById(id);
        if (medPlans.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ID not found!");
        }
        /*String etag = generateETag(medPlans.toString());
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setETag(etag);*/
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new JSONObject(medPlans).toString());
        }else{
        String errorMessage = "{\"message\":\"Incorrect ETag!\"}";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorMessage);
    }}
    @DeleteMapping("/medplans/{id}")
    public ResponseEntity<String> deleteMedPlan(@PathVariable("id") String id,@RequestHeader(value="Authorization", required = false) String bearerToken,@RequestHeader(value="If-Match") String im) throws MedPlanNotFoundException {

        if (!oauth.verifier(bearerToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Invalid Access Token\"}");
        }
        String etag = medPlanService.fetchETag(id);
        if (etag == null || etag.isEmpty()) {
            String errorMessage = "{\"message\":\"Pass appropriate objectId or ETag!\"}";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorMessage);
        }

        if (im.equals("")) {
            String errorMessage = "{\"message\":\"Pass appropriate ETag!\"}";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorMessage);
        } else if (medPlanService.fetchETag(id).equals(im)) {


            boolean result = medPlanService.deleteMedPlan(id);
            if (result) {

               /* Map<String, Object> plan = medPlanService.fetchMedPlanById(id);
                Map<String, String> message = new HashMap<String, String>();
                message.put("operation", "DELETE");
                message.put("body",  new JSONObject(plan).toString());

                System.out.println("Sending message: " + message);
                template.convertAndSend(MedPlanApp.queueName, message);*/

                return ResponseEntity.ok("Plan deleted Successfully!");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ID not found!");
            }


        }else{
            String errorMessage = "{\"message\":\"Incorrect ETag!\"}";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorMessage);
        }
    }
    @PatchMapping("/medplans/{id}")
    public ResponseEntity<String> patchMedPlanById(@RequestBody String updatedMedPlans,@PathVariable("id") String id,@RequestHeader(value="Authorization", required = false) String bearerToken,@RequestHeader(value="If-Match") String im) throws MedPlanNotFoundException, IOException {
        if (!oauth.verifier(bearerToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Invalid Access Token\"}");
        }
        Map<String, Object> medPlans;
        medPlans = medPlanService.fetchMedPlanById(id);
        if (medPlans != null) {
            String fetchedEtag = medPlanService.fetchETag(id);
            if (fetchedEtag == null || fetchedEtag.isEmpty()) {
                String errorMessage = "{\"message\":\"Pass appropriate objectId or ETag!\"}";
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorMessage);
            }
            if(im.equals("")){
                String errorMessage = "{\"message\":\"Pass apprpriate ETag!\"}";
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorMessage);
            }else if(medPlanService.fetchETag(id).equals(im)){
                try (InputStream inputStream = getClass().getResourceAsStream("/JSONSchema.json")) {
                    JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
                    Schema schema = SchemaLoader.load(rawSchema);
                    schema.validate(new JSONObject(updatedMedPlans));
                }
                JSONObject updatedJson = new JSONObject(updatedMedPlans);
                //JSONObject oldJson = new JSONObject(medPlans);
        String result = medPlanService.saveMedPlan(id,updatedJson);

        if (result.equals("true"))   {

            Map<String, String> message = new HashMap<String, String>();
            message.put("operation", "SAVE");
            message.put("body", updatedMedPlans);

            System.out.println("Sending message: " + message);
            template.convertAndSend(MedPlanApp.queueName, message);

        String etag = generateETag(medPlans.toString());
            medPlanService.saveETag(id,etag);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setETag(etag);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(responseHeaders)
                .body("Object Id: "+id+" patched successfully!");
    } else {
            String errorMessage = "{\"message\":\"No Data Found!\"}";
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorMessage);
        }
         }else{
             String errorMessage = "{\"message\":\"Incorrect ETag!\"}";
             return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                     .contentType(MediaType.APPLICATION_JSON)
                     .body(errorMessage);
         }


}
        return null;


    }

  /*  @PutMapping("/medplans/{id}")
    public ResponseEntity<String> putMedPlanById(@RequestBody String updatedMedPlans,@PathVariable("id") String id,@RequestHeader(value="Authorization", required = false) String bearerToken,@RequestHeader(value="If-Match") String im) throws MedPlanNotFoundException, IOException {
        if (!oauth.verifier(bearerToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Invalid Access Token\"}");
        }
        Map<String, Object> medPlans;
        medPlans = medPlanService.fetchMedPlanById(id);
        if (medPlans != null) {
            if(im.equals("")){
                String errorMessage = "{\"message\":\"Pass apprpriate ETag!\"}";
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorMessage);
            }else if(medPlanService.fetchETag(id).equals(im)){
                JSONObject updatedJson = new JSONObject(updatedMedPlans);
                String result = medPlanService.saveMedPlan(id,updatedJson);
                if (result.equals("true"))  {

                    try (InputStream inputStream = getClass().getResourceAsStream("/JSONSchema.json")) {
                        JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
                        Schema schema = SchemaLoader.load(rawSchema);
                        schema.validate(new JSONObject(updatedMedPlans));
                    }
                    String etag = generateETag(medPlans.toString());

                    HttpHeaders responseHeaders = new HttpHeaders();
                    responseHeaders.setETag(etag);
                    return ResponseEntity.status(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .headers(responseHeaders)
                            .body("Object Id: "+id+" patched successfully!");
                } else {
                    String errorMessage = "{\"message\":\"No Data Found!\"}";
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(errorMessage);
                }
            }else{
                String errorMessage = "{\"message\":\"Incorrect ETag!\"}";
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorMessage);
            }


        }
        return null;


    }*/

    }

