package com.restapi.repository;
public interface MedPlansDao {
    boolean saveMedplan(String medPlans);

    String fetchAllMedPlans();

    String fetchMedPlanById(String id);

    boolean deleteMedPlan(String id);
}
