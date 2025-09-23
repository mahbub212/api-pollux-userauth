package id.co.msiglife.pollux.repository;

import id.co.msiglife.pollux.composite.CompRoleAccessMenu;
import id.co.msiglife.pollux.entity.PolluxRoleAccessMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleAccessMenuRepository extends JpaRepository<PolluxRoleAccessMenu, CompRoleAccessMenu>{

	@Query(	nativeQuery = true,
			value = "SELECT * FROM EKA.LST_POLLUX_ROLE_ACCESS_MENU WHERE FLAG_AKTIF = 1 AND ROLE_ID = :roleId ")
	List<PolluxRoleAccessMenu> getRoleAccessMenu(int roleId);

	@Query(
			nativeQuery = true,
			value = "SELECT * FROM EKA.LST_POLLUX_ROLE_ACCESS_MENU WHERE FLAG_AKTIF = 1 AND ROLE_ID IN (:roleIds) AND (MENU_ID, ROLE_ID) IN ( " +
					"        SELECT MENU_ID, MIN(ROLE_ID) " +
					"        FROM EKA.LST_POLLUX_ROLE_ACCESS_MENU " +
					"        WHERE FLAG_AKTIF = 1 " +
					"          AND ROLE_ID IN (:roleIds) " +
					"        GROUP BY MENU_ID " +
					"    )"
	)
	List<PolluxRoleAccessMenu> getRoleAccessMenu(@Param("roleIds") List<Integer> roleIds);


}
