package com.restapi.service;

import com.restapi.model.MedPlans;
import com.restapi.repository.MedPlansDao;
import com.restapi.validation.exceptions.MedPlanNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedPlanServiceImpl implements MedPlanService{

    @Autowired
    private MedPlansDao medPlansDao;
    @Override
    public boolean saveMedPlan(String medPlans) {
        return medPlansDao.saveMedplan(medPlans);
    }

    @Override
    public String fetchAllMedPlans() {
        return medPlansDao.fetchAllMedPlans();
    }

    @Override
    public String fetchMedPlanById(String id) throws MedPlanNotFoundException {
        String medPlans=medPlansDao.fetchMedPlanById(id);
        if(medPlans==null){
            throw new MedPlanNotFoundException("MedPlan not found with id: "+id);
        }else{
            return medPlans;
        }
    }

    @Override
    public boolean deleteMedPlan(String id) {
        return medPlansDao.deleteMedPlan(id);
    }
}
