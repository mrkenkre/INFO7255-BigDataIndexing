package com.restapi.repository;
import com.restapi.validation.exceptions.MedPlanNotFoundException;
import org.json.JSONObject;

import java.util.Map;

public interface MedPlansDao {
    String saveMedplan(String key, JSONObject plans);

    String fetchAllMedPlans();

    Map<String, Object> fetchMedPlanById(String id);

    boolean deleteMedPlan(String id) throws MedPlanNotFoundException;
    boolean saveETag(String id, String etag);
    String fetchETag(String id);
}
