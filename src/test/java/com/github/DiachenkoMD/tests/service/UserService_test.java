package com.github.DiachenkoMD.tests.service;

import com.github.DiachenkoMD.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.sevices.UsersService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class})
public class UserService_test {
    @Mock
    private UsersDAO _usersDao;

    @InjectMocks
    private UsersService usersService;


}
