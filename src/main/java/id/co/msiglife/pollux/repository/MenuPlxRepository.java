package id.co.msiglife.pollux.repository;

import id.co.msiglife.pollux.entity.MenuPlx;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface MenuPlxRepository extends JpaRepository<MenuPlx, Long> {
    @Query(value = "WITH menu_tree AS ( " +
            "    SELECT m.LSMP_ID, m.MENU_ID, m.PARENT_ID, m.LABEL, m.TYPE, m.PATH, m.ICON, m.CREATED_DATE, m.DELETED_DATE, SYS_CONNECT_BY_PATH(m.LSMP_ID, '>') AS PATH_IDS " +
            "    FROM EKA.LST_MENU_PLX m " +
            "    WHERE m.DELETED_DATE IS NULL " +
            "    START WITH m.PARENT_ID IS NULL " +
            "    CONNECT BY PRIOR m.LSMP_ID = m.PARENT_ID " +
            ") " +
            "SELECT mt.* " +
            "FROM menu_tree mt " +
            "JOIN EKA.LST_POLLUX_ROLE_ACCESS_MENU ram ON mt.MENU_ID = ram.MENU_ID AND ram.ROLE_ID IN (:roleIds) AND ram.FLAG_AKTIF = 1 " +
            "ORDER BY mt.PATH_IDS" ,
            nativeQuery = true)
    List<Map<String, Object>> findMenuTreeByRoles(@Param("roleIds") List<Integer> roleIds);
}
