package com.restapi.service;

import com.restapi.validation.exceptions.MedPlanNotFoundException;
import org.json.JSONObject;

import java.util.Map;

public interface MedPlanService {
    String saveMedPlan(String key, JSONObject plans);

    String fetchAllMedPlans();

    Map<String, Object> fetchMedPlanById(String id) throws MedPlanNotFoundException;

    boolean deleteMedPlan(String id) throws MedPlanNotFoundException;
    boolean saveETag(String id, String etag);
    String fetchETag(String id);
}
