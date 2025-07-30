package id.co.msiglife.pollux.repository;

import id.co.msiglife.pollux.composite.CompUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import id.co.msiglife.pollux.entity.UserRole;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, CompUserRole> {

	@Query("SELECT ur FROM UserRole ur WHERE ur.lusId = :lusId AND EXISTS " +
			"(SELECT DISTINCT ura.roleId FROM PolluxRoleAccessMenu ura WHERE ura.roleId = ur.roleId)")
	UserRole findByLusIdWithRoleAccess(@Param("lusId") Long lusId);

	@Query("SELECT ur FROM UserRole ur WHERE ur.lusId = :lusId AND EXISTS " +
			"(SELECT DISTINCT ura.roleId FROM PolluxRoleAccessMenu ura WHERE ura.roleId = ur.roleId)")
	List<UserRole> findByLusIdWithRoleAccessList(@Param("lusId") Long lusId);
		
}
