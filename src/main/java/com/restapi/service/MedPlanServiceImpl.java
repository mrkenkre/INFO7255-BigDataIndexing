package com.restapi.service;

import com.restapi.repository.MedPlansDao;
import com.restapi.validation.exceptions.MedPlanNotFoundException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MedPlanServiceImpl implements MedPlanService{

    @Autowired
    private MedPlansDao medPlansDao;
    @Override
    public String saveMedPlan(String key, JSONObject medPlans) {
        return medPlansDao.saveMedplan(key,medPlans);
    }

    @Override
    public String fetchAllMedPlans() {
        return medPlansDao.fetchAllMedPlans();
    }

    @Override
    public Map<String, Object> fetchMedPlanById(String id) throws MedPlanNotFoundException {
        Map<String, Object> medPlans=medPlansDao.fetchMedPlanById(id);
        if(medPlans==null){
            throw new MedPlanNotFoundException("MedPlan not found with id: "+id);
        }else{
            return medPlans;
        }
    }

    @Override
    public boolean deleteMedPlan(String id) throws MedPlanNotFoundException {
        return medPlansDao.deleteMedPlan(id);
    }

    @Override
    public boolean saveETag(String id, String etag) {
        return medPlansDao.saveETag(id,etag);
    }


    @Override
    public String fetchETag(String id) {
        return medPlansDao.fetchETag(id);
    }
}
