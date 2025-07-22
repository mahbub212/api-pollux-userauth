package id.co.msiglife.pollux.repository;

import id.co.msiglife.pollux.entity.MenuPlx;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface MenuPlxRepository extends JpaRepository<MenuPlx, Long> {
    @Query(value = "WITH menu_tree AS ( " +
            "    SELECT m.LSMP_ID, m.MENU_ID, m.PARENT_ID, m.LABEL, m.TYPE, m.PATH, m.ICON, m.CREATED_DATE, m.DELETED_DATE " +
            "    FROM EKA.LST_MENU_PLX m " +
            "    WHERE m.DELETED_DATE IS NULL " +
            "), access_menu AS ( " +
            "    SELECT DISTINCT ram.MENU_ID " +
            "    FROM EKA.LST_POLLUX_ROLE_ACCESS_MENU ram " +
            "    WHERE ram.ROLE_ID IN (:roleIds) AND ram.FLAG_AKTIF = 1 " +
            ") " +
            "SELECT mt.*, CASE WHEN am.MENU_ID IS NOT NULL THEN 1 ELSE 0 END AS IS_ACCESSIBLE " +
            "FROM menu_tree mt " +
            "LEFT JOIN access_menu am ON mt.MENU_ID = am.MENU_ID " +
            "ORDER BY mt.PARENT_ID asc, mt.LSMP_ID desc " ,
            nativeQuery = true)
    List<Map<String, Object>> findMenuTreeByRoles(@Param("roleIds") List<Integer> roleIds);
}
