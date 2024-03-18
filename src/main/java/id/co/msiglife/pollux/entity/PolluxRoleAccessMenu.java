package id.co.msiglife.pollux.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import id.co.msiglife.pollux.composite.CompRoleAccessMenu;
import lombok.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@IdClass(CompRoleAccessMenu.class)
@Table(name = "LST_POLLUX_ROLE_ACCESS_MENU", schema = "EKA")
public class PolluxRoleAccessMenu {
	
	@Id
	@Column(name = "ROLE_ID")
	private Long roleId;

	@Id
	@Column(name = "MENU_ID")
	private Integer menuId;

	@Column(name = "FLAG_AKTIF")
	private Integer flag;

	@Column(name = "LUS_INPUT")
	private Integer input;

	@Column(name = "LAST_UPDATED")
	@JsonFormat(pattern="dd-MM-yyyy HH:mm:ss", timezone="Asia/Jakarta")
	private Date lastUpdated;

	@Column(name = "LUS_UPDATED")
	private Integer updated;

	@Column(name = "LRAM_VIEW")
	private Integer view;

	@Column(name = "LRAM_ADD")
	private Integer add;

	@Column(name = "LRAM_EDIT")
	private Integer edit;

	@Column(name = "LRAM_DELETE")
	private Integer delete;

	@Column(name = "LRAM_DOWNLOAD")
	private Integer download;
	
	@Column(name = "LRAM_APPROVAL")
	private Integer approve;

	@Column(name = "LRAM_CHANGE")
	private Integer change;

}
