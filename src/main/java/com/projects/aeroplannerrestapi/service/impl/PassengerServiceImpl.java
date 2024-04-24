package com.projects.aeroplannerrestapi.service.impl;

import com.projects.aeroplannerrestapi.dto.PassengerResponse;
import com.projects.aeroplannerrestapi.dto.UserDto;
import com.projects.aeroplannerrestapi.entity.User;
import com.projects.aeroplannerrestapi.enums.RoleEnum;
import com.projects.aeroplannerrestapi.exception.ResourceNotFoundException;
import com.projects.aeroplannerrestapi.mapper.UserMapper;
import com.projects.aeroplannerrestapi.repository.UserRepository;
import com.projects.aeroplannerrestapi.service.PassengerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PassengerServiceImpl implements PassengerService {

    private final UserRepository userRepository;

    @Override
    public PassengerResponse getPassengers(int pageNumber, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortBy.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sort);
        Page<User> page = userRepository.findByRole(RoleEnum.USER, pageable);
        List<UserDto> passengers = page.getContent().stream()
                .map(UserMapper.INSTANCE::userToUserDto)
                .collect(Collectors.toList());
        PassengerResponse passengerResponse = new PassengerResponse();
        passengerResponse.setContent(Collections.singletonList(passengers));
        passengerResponse.setPageNumber(page.getNumber());
        passengerResponse.setPageSize(page.getSize());
        passengerResponse.setTotalPages(page.getTotalPages());
        passengerResponse.setTotalElements(page.getTotalElements());
        passengerResponse.setLast(page.isLast());
        return passengerResponse;
    }

    @Override
    public UserDto getPassenger(Long id) {
        return userRepository.findByIdAndRolesName(id, RoleEnum.USER)
                .map(UserMapper.INSTANCE::userToUserDto)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger", "id", id.toString()));
    }

    @Override
    public void deletePassenger(Long id) {
        if (!userRepository.existsByIdAndRoles_Name(id, RoleEnum.USER)) {
            throw new ResourceNotFoundException("Passenger", "id", id.toString());
        }
        userRepository.deleteByIdAndRoles_Name(id, RoleEnum.USER);
    }
}