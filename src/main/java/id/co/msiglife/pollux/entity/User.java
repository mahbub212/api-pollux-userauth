package id.co.msiglife.pollux.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "LST_USER", schema = "EKA")
public class User {

	@Id
	@Column(name = "LUS_ID")
	private Long userId;
	
	@Column(name = "MCL_ID")
	private String mclId;
	
	@Column(name = "LDE_ID")
	private String ldeId;
	
	@Column(name = "LCA_ID")
	private String lcaId;
	
	@Column(name = "LUS_LOGIN_NAME")
	private String username;

	@Column(name = "LUS_PASSWORD")
	private String password;
	
	@Column(name = "LUS_ADMIN")
	private Integer lusAdmin;
	
	@Column(name = "LUS_ACTIVE")
	private Integer active;
	
	@Column(name = "LUS_NIK")
	private String nik;

	@Column(name = "LUS_EMAIL")
	private String email;

	@Column(name = "LUS_FULL_NAME")
	private String lusFullName;

	@Column(name = "LUSS_ONLINE")
	private Date lussOnline;
	
	@Column(name = "LUS_MINE")
	private Integer lusMine;
	
	@Column(name = "LUS_CHANGE")
	private Date lusChange;

	@Column(name = "CAB_BANK")
	private String cabBank;
	
	@Column(name = "VALID_BANK_1")
	private Integer validBank1;
	
	@Column(name = "VALID_BANK_2")
	private Integer validBank2;
	
	@Column(name = "JN_BANK")
	private Integer jnBank;
	
	@Column(name = "FLAG_APPROVE")
	private Integer flagApprove;
	
	@Column(name = "LUS_DEFAULT_PASSWORD")
	private String lusDefaultPassword;

	@Column(name = "LUS_BAS")
	private Integer lusBas;
	
	@Column(name = "LUS_OSUSER")
	private String lusOsuser;

	@Column(name = "LUS_MACHINE")
	private String lusMachine;

	@Column(name = "LUS_PROGRAM")
	private String lusProgram;

	@Column(name = "LSB_ID")
	private Integer lsbId;
	
	@Column(name = "FLAG_AKM")
	private Integer flagAkm;
	
	@Column(name = "LUS_SPECIAL")
	private Integer lusSpecial;
	
	@Column(name = "LUS_TTD")
	private String lusttd;
	
	@Column(name = "LUS_MSAG_ID")
	private String lusMsagId;
	
	@Column(name = "LUS_NON_ACTIVE")
	private Date lusNonActive;
	
}
