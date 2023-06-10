package com.restapi.service;

import com.restapi.model.MedPlans;
import com.restapi.validation.exceptions.MedPlanNotFoundException;

import java.util.List;

public interface MedPlanService {
    boolean saveMedPlan(String medPlans);

    String fetchAllMedPlans();

    String fetchMedPlanById(String id) throws MedPlanNotFoundException;

    boolean deleteMedPlan(String id);
}
