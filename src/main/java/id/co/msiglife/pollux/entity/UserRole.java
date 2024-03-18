package id.co.msiglife.pollux.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import id.co.msiglife.pollux.composite.CompUserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@IdClass(CompUserRole.class)
@Table(name = "LST_USER_ROLE", schema = "EKA")
public class UserRole {
	
	@Id
	@Column(name = "LUS_ID")
	private Long lusId;
	
	@Id
	@Column(name = "ROLE_ID")
	private Integer roleId;
	
	@Column(name = "FLAG_AKTIF")
	private Integer flagAktif;

	@Column(name = "FLAG_CUTI")
	private Integer flagCuti;

}
