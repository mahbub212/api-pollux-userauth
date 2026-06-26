package id.co.msiglife.pollux.repository;

import id.co.msiglife.pollux.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
	
	Menu findByMenuId(Long menuId);

    @Query(value = "WITH menu_tree AS ( " +
            "    SELECT " +
            "       CASE WHEN m.category IS NULL THEN '0' ELSE m.category END AS parent_id, " +
            "       m.MENU_ID, m.NAMA_MENU as LABEL, m.MENU_CODE AS TYPE, m.PATH_ICON AS PATH, m.menu_icon as ID " +
            "    FROM EKA.LST_MENU m " +
            "    WHERE m.JENIS = 111 and m.FLAG_AKTIF = 1 " +
            "), access_menu AS ( " +
            "    SELECT DISTINCT ram.MENU_ID " +
            "    FROM EKA.LST_POLLUX_ROLE_ACCESS_MENU ram " +
            "    WHERE ram.ROLE_ID IN (:roleIds) AND ram.FLAG_AKTIF = 1 " +
            ") " +
            "SELECT mt.*, CASE WHEN am.MENU_ID IS NOT NULL AND mt.MENU_ID IS NOT NULL THEN 1 ELSE 0 END AS IS_ACCESSIBLE " +
            "FROM menu_tree mt " +
            "LEFT JOIN access_menu am ON mt.MENU_ID = am.MENU_ID " +
            "ORDER BY mt.PARENT_ID, mt.MENU_ID " ,
            nativeQuery = true)
    List<Map<String, Object>> findMenuTreeByRoles(@Param("roleIds") List<Integer> roleIds);

}
