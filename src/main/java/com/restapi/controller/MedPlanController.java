package com.restapi.controller;
import com.restapi.model.MedPlans;
import com.restapi.service.MedPlanService;
import com.restapi.validation.exceptions.MedPlanNotFoundException;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
public class MedPlanController {
    @Autowired
    private MedPlanService medPlanService;
    @PostMapping("/medplans")
    public ResponseEntity<String> savePlan(@RequestBody String medPlans, @RequestHeader HttpHeaders headers) throws IOException {

        try (InputStream inputStream = getClass().getResourceAsStream("/JSONSchema.json")) {
            JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
            Schema schema = SchemaLoader.load(rawSchema);
            schema.validate(new JSONObject(medPlans));
        }
        boolean result = medPlanService.saveMedPlan(medPlans);
        if (result) {
            return ResponseEntity.ok("Plan created Successfully!");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    @GetMapping(value = "/medplans", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> fetchAllMedPlans() {

        String medPlans = medPlanService.fetchAllMedPlans();
            if (medPlans != null) {
                return ResponseEntity.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(medPlans);
            } else {
                String errorMessage = "{\"message\":\"No Data Found!\"}";
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorMessage);
            }
        }


    @GetMapping("/medplans/{id}")
    public ResponseEntity<String> fetchMedPlanById(@PathVariable("id") String id) throws MedPlanNotFoundException {

        String medPlans;
        medPlans = medPlanService.fetchMedPlanById(id);
        return ResponseEntity.ok(medPlans);
    }
    @DeleteMapping("/medplans/{id}")
    public ResponseEntity<String> deleteMedPlan(@PathVariable("id") String id) {

        boolean result = medPlanService.deleteMedPlan(id);
        if (result) {
            return ResponseEntity.ok("Plan deleted Successfully!");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ID not found!");
        }
    }
}
