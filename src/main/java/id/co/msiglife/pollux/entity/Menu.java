package id.co.msiglife.pollux.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "LST_MENU", schema = "EKA")
public class Menu {
	
	@Id
	@Column(name = "MENU_ID")
	private Long menuId;
	
	@Column(name = "JENIS")
	private Long jenis;
	
	@Column(name = "NAMA_MENU")
	private String namaMenu;
	
	@Column(name = "FLAG_AKTIF")
	private Integer flagAktif;
	
	@Column(name = "MENU_CODE")
	private String menuCode;

	@Column(name = "CATEGORY")
	private String category;
	
	@Column(name = "MENU_ICON")
	private String menuIcon;
	
	@Column(name = "PATH_ICON")
	private String pathIcon;

}
