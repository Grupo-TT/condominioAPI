package com.condominio.service;

import com.condominio.persistence.model.RoleEntity;
import com.condominio.persistence.model.RoleEnum;
import com.condominio.persistence.repository.RoleRepository;
import com.condominio.service.implementation.RolService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RolServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RolService rolService;

    private AutoCloseable closeable;
    private RoleEntity roleEntity;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        roleEntity = new RoleEntity();
        roleEntity.setId(1L);
        roleEntity.setRoleEnum(RoleEnum.ADMIN);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testFindByRoleEnum_WhenRoleExists() {

        when(roleRepository.findByRoleEnum(RoleEnum.ADMIN)).thenReturn(Optional.of(roleEntity));


        Optional<RoleEntity> result = rolService.findByRoleEnum(RoleEnum.ADMIN);


        assertThat(result).isPresent();
        assertThat(result).map(RoleEntity::getRoleEnum).hasValue(RoleEnum.ADMIN);

        verify(roleRepository).findByRoleEnum(RoleEnum.ADMIN);
    }

    @Test
    void testFindByRoleEnum_WhenRoleDoesNotExist() {

        when(roleRepository.findByRoleEnum(RoleEnum.ARRENDATARIO)).thenReturn(Optional.empty());


        Optional<RoleEntity> result = rolService.findByRoleEnum(RoleEnum.ARRENDATARIO);


        assertThat(result).isNotPresent();
        verify(roleRepository).findByRoleEnum(RoleEnum.ARRENDATARIO);
    }
}