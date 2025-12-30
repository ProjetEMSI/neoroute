package com.neoroute.logistics.repository;

import com.neoroute.logistics.domain.user.Role;
import com.neoroute.logistics.domain.user.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity
 */
@Repository
public interface UserRepository extends Neo4jRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);

    List<User> findByRoleAndActiveTrue(Role role);

    @Query("MATCH (u:User) WHERE u.role = $role RETURN u ORDER BY u.averageScore DESC LIMIT 10")
    List<User> findTopDriversByScore(Role role);

    long countByRole(Role role);
}
