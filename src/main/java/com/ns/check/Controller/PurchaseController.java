package com.ns.check.Controller;

import com.ns.check.Model.Purchase;
import com.ns.check.Model.ResultantModel;
import com.ns.check.Repository.PurchaseRepo;
import com.ns.check.Service.Java.PurchaseProcessJava;
import com.ns.check.Service.Drools.PurchaseServiceDrools;
import com.ns.check.Service.WatchService.WatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/api")
public class PurchaseController {


    @Autowired
    private PurchaseRepo purchaseRepo;

    @Autowired
    private PurchaseServiceDrools purchaseServiceDrools;

    @Autowired
    private PurchaseProcessJava purchaseServiceJava;

    @Autowired
    private WatchService watchService;

    @PostMapping("/purchase")
    public String submitPurchase() {

        new Thread(() -> {
            try {
                watchService.watchingMethod();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        List<Purchase> purchaseData1 = new ArrayList<>();
        List<ResultantModel> resultSet = new ArrayList<>();


        purchaseData1 = purchaseRepo.findAll();
        purchaseServiceDrools.firstDone();

        purchaseServiceJava.firstDone();

        for (Purchase data : purchaseData1) {

            if (data.getId() < 10) {

                purchaseServiceDrools.processingMethod(data, resultSet);
            }

//                purchaseServiceJava.processingMethod(data, resultSet);

//            }

        }

        purchaseServiceDrools.dataStore(resultSet);
//        purchaseServiceJava.dataStore(resultSet);
        return "Done from major method";

    }
}


//    For Getting all records


//    @GetMapping(value = "/records")
//    public List<ResultantModel> getAllData()
//    {
//        return resultRepo.findAll();
//    }


//    Get All records by the OrderNumber


//    @GetMapping(value = "/records/{orderNumber}")
//    public List<ResultantModel> getParticularData(@PathVariable("orderNumber") String orderNumber)
//    {
//        return resultRepo.findByOrderNumber(orderNumber);
//    }

//    public void resultantData(ResultantModel rm, String orderNumber) {
//        rm.setOrderNumber(orderNumber);
//
//
//
//        resultRepo.save(rm);
//
//    }
