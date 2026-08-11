package com.lea.transport.service;

import com.lea.transport.model.BusContractor;
import com.lea.transport.model.Contract;

import java.util.List;
import java.util.stream.Collectors;

/** LEA-only functionality: managing contracts and reviewing contractor performance. */
public class ContractService {
    private final DataRepository repository;

    public ContractService(DataRepository repository) { this.repository = repository; }

    public void addContract(Contract contract) { repository.addContract(contract); }

    public List<BusContractor> listUnderperformingContractors() {
        return repository.getContractors().values().stream()
                .filter(BusContractor::isUnderperforming)
                .collect(Collectors.toList());
    }
}
