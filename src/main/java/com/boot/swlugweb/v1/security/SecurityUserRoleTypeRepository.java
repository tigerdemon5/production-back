package com.boot.swlugweb.v1.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityUserRoleTypeRepository extends JpaRepository<SecurityUserRoleTypeDomain, String> {

}
