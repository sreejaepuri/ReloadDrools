package com.ns.check.Service.Drools;

import com.ns.check.Model.Purchase;
import com.ns.check.Model.ResultantModel;
import com.ns.check.Model.UserDetails;
import com.ns.check.Repository.PurchaseRepo;
import com.ns.check.Repository.ResultantModelRepo;
import com.ns.check.Repository.UserDetailRepo;
import com.ns.check.Service.WatchService.WatchService;
import org.kie.api.KieServices;
import org.kie.api.builder.*;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


@Service
public class PurchaseServiceDrools {

    @Autowired
    private PurchaseRepo purchaseRepo;


    @Autowired
    private UserDetailRepo userRepo;

    @Autowired
    private ResultantModelRepo resultRepo;

    @Autowired
    private WatchService watchService;


    HashMap<Integer, Integer> hm = new HashMap<>();

    HashMap<Integer, Integer> distLevel = new HashMap<>();


    public void firstDone() {
        List<Integer> distributor = userRepo.findDataAll();
        List<Integer> sponser = userRepo.findDataAll1();
        int distCap = distributor.size();

        for (int i = 0; i < distCap; i++) {
            hm.put(distributor.get(i), sponser.get(i));
        }
//        System.out.println(hm);

        List<Integer> level = userRepo.findDataLevel();

        for (int i = 0; i < distCap; i++) {
            distLevel.put(distributor.get(i), level.get(i));
        }

//        System.out.println(hm);
//        System.out.println(distLevel);
    }


    public void processingMethod(Purchase data, List<ResultantModel> resultSet) {
        int distributorId = data.getDistributorId();
        List<Integer> distributorData = new ArrayList<>();
        distributorData.add(distributorId);

        while (distributorId >= 1) {
            distributorId = hm.get(distributorId);
            distributorData.add(distributorId);
        }
        System.out.println("******");
        System.out.println(distributorData);

        List<UserDetails> dataFromDb = new ArrayList<>();
        dataFromDb = userRepo.findByDistributorIdIn(distributorData);

//        System.out.println(dataFromDb);


        dataProcessing(dataFromDb, data, resultSet);
        distributorData.clear();

    }

    private void dataProcessing(List<UserDetails> data, Purchase purchasedata, List<ResultantModel> resultSet) {
        Collections.reverse(data);
        int size = data.size();
        List<UserDetails> userData = new ArrayList<>();

        UserDetails ud = new UserDetails();

        if (size == 1) {
            ud = data.get(0);
            ud.setNextLevel(0);
            userData.add(ud);
            System.out.println(userData);
        } else {
            for (int i = 0; i < size; i++) {
                if (i == 0) {
                    ud = data.get(i);
                    ud.setNextLevel(0);
                    userData.add(ud);
                } else if (data.get(i).getDistributorId() == 1) {
                    ud = data.get(i);
//                ud.setNextLevel(userData.get(size-1).getLevel());
//                userData.add(ud);
                    ud.setNextLevel(userData.get(userData.size() - 1).getLevel());
                    userData.add(ud);
                } else {
                    ud = data.get(i);
                    ud.setNextLevel(userData.get(userData.size() - 1).getLevel());
                    userData.add(ud);
                }

            }


        }

        for (UserDetails finishedData : userData) {
            long startTime = System.nanoTime();
            commisionCalculation(finishedData, purchasedata, resultSet);
            long endTime = System.nanoTime();

//            System.out.println(finishedData);
        }

    }


    public void commisionCalculation(UserDetails userDetails, Purchase purchaseData, List<ResultantModel> resultSet) {
        KieServices kieServices = KieServices.Factory.get();

        if (watchService.getCount() > 0) {
            KieFileSystem kfs = kieServices.newKieFileSystem();

            kfs.write("src/main/resources/rules/rules.drl", loadRules());

            KieBuilder kieBuilder = kieServices.newKieBuilder(kfs);
            kieBuilder.buildAll();
            KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());

//            KieSession kieSession = kieContainer.newKieSession("ksession-rule");
            StatelessKieSession kieSession = kieContainer.newStatelessKieSession();

            kieSession.setGlobal("ccAmount", purchaseData.getCc());
            kieSession.setGlobal("orderAmount", purchaseData.getOrderPrice());


            ResultantModel rm = new ResultantModel();
            kieSession.setGlobal("rm", rm);


            String level = String.valueOf(userDetails.getLevel());

//            kieSession.getAgenda().getAgendaGroup(level).setFocus();


//            kieSession.insert(userDetails);
//            kieSession.fireAllRules();
            kieSession.execute(userDetails);

            rm.setOrderNumber(purchaseData.getOrderNumber());
            if (rm.getDistributorId() > 1) {
                int sponserLevel = distLevel.get(userDetails.getSponserid());
                rm.setSponserLevel(sponserLevel);
            } else {
                rm.setSponserLevel(0);
            }


            resultSet.add(rm);

            System.out.println("The rule file is  modified  & it is from the commission calculation method");
            watchService.setCount(0);
        } else {

            KieContainer kcontainer = kieServices.getKieClasspathContainer();
//            KieSession ksession = kcontainer.newKieSession("ksession-rule");
//            ReleaseId releaseId = ks.newReleaseId("com.ns", "check", "0.0.1-SNAPSHOT");
//            KieContainer kcontainer = ks.newKieContainer(releaseId);


            StatelessKieSession ksession = kcontainer.newStatelessKieSession();

            ksession.setGlobal("ccAmount", purchaseData.getCc());
            ksession.setGlobal("orderAmount", purchaseData.getOrderPrice());


            ResultantModel rm = new ResultantModel();
            ksession.setGlobal("rm", rm);


            String level = String.valueOf(userDetails.getLevel());

//            ksession.getAgenda().getAgendaGroup(level).setFocus();


            ksession.execute(userDetails);

//            ksession.insert(userDetails);


            long startTime = System.nanoTime();
//            int firedRules = ksession.fireAllRules();
            long endTime = System.nanoTime();
//        System.out.println("Time to fire the rule" + (endTime - startTime));


            rm.setOrderNumber(purchaseData.getOrderNumber());
            if (rm.getDistributorId() > 1) {
                int sponserLevel = distLevel.get(userDetails.getSponserid());
                rm.setSponserLevel(sponserLevel);
            } else {
                rm.setSponserLevel(0);
            }


            resultSet.add(rm);
        }

//            System.gc();


    }



    public String loadRules() {

        String content = null;
        try {
            content = new String(Files.readAllBytes(Paths.get("E:\\Reload Check\\src\\main\\resources\\Rules\\rules.drl")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content;
    }


    public void dataStore(List<ResultantModel> data) {
        resultRepo.saveAll(data);
//        new Thread() {
//            public void run() {
//                resultRepo.saveAll(data);
//            }
//        }.start();
    }
}








