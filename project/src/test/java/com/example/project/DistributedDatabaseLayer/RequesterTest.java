package com.example.project.DistributedDatabaseLayer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequesterTest {

    Requester requester = Requester.getInstance();

    @Test
    void requestVMDBCheck() {
        // can check only when the vm is available, not possible with one vm
        //assertFalse(requester.requestVMDBCheck("shiva"));
    }
}