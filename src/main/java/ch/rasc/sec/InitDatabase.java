package ch.rasc.sec;

import ch.rasc.sec.cipher.AES;
import ch.rasc.sec.cipher.DiffieHellman;
import ch.rasc.sec.cipher.RSA;
import ch.rasc.sec.model.User;
import ch.rasc.sec.model.UserGroup;
import ch.rasc.sec.repository.GrantsRepository;
import ch.rasc.sec.repository.UserGroupRepository;
import ch.rasc.sec.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class InitDatabase implements ApplicationListener<ContextRefreshedEvent> {

	private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

	@Autowired
	private UserGroupRepository groupRepository;

	@Autowired
	private GrantsRepository docGroupRepository;



	@Autowired
	public InitDatabase(PasswordEncoder passwordEncoder, UserRepository userRepository) {
		this.passwordEncoder = passwordEncoder;
	}



	@Override
	@Transactional
	public void onApplicationEvent(ContextRefreshedEvent event) {

		//InputStream stream = GoogleApiServiceImpl.class.getResourceAsStream(GoogleAuth.KEY_FILENAME);

		try {
			GoogleAuth.serverGoogleKey = AES.generateKey();
			GoogleAuth.ivectorGoogle = new IvParameterSpec(AES.generateIV(GoogleAuth.serverGoogleKey));
			DiffieHellman.generateDHParamSpec();
			GoogleAuth.dhKeyPair = DiffieHellman.generateKeyPair();
			GoogleAuth.keyAgreement = DiffieHellman.generateKeyAgreement(GoogleAuth.dhKeyPair);
			GoogleAuth.rsaKeyPair = RSA.generateKeyPair();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidParameterSpecException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}

		if (this.userRepository.count() == 0) {
			User adminUser = new User();
			adminUser.setEmail("superadmin@gmail.com");
			adminUser.setSecret("IB6EFEQKE7U2TQIB");
			adminUser.setTries(0);
			adminUser.setPassword(passwordEncoder.encode("password"));
			userRepository.save(adminUser);
			User seorgy = new User("sergio.kiselev509@gmail.com", passwordEncoder.encode("password"), null, 0,new BigInteger("199582142956008510570996053172578267554407915993617735369142167915397142162101702274261155006143481050964536906357859868500265309938528437730131957888293279531902393921594591047895961829712485440709200434727036916806944676936506666438100089796501272431044144260592539417976340184207951204411838110300199255833924775384022250296014405309976711389266143361136367590395318293788212870369891509475107972886584438227318559345213284012430679424459625903670766427650442713998028888307588791225335594894607733639423134360833104429092978718666008266725909040776198803684871360685158919887799944927460463169301358572226629752702204501631537084714011103840020743824891137789850955483169245625582401264004147787310628865").toByteArray());
			userRepository.save(seorgy);
			User denis = new User("m-den-i@yandex.by", passwordEncoder.encode("password"), null,0,new BigInteger("199582142956008510570996053172578267554407915993617735369142167915397142162101701913823262653681100023983150321137792204934508981415995600191965909229667494654445531798799950731183949150663685713567473323423574982386757119901513407822992699999981890741450758816243559985512000328886001833887655168286282441540896112322682789146473986999338953025339445041389533747565852931283528453395512551004578459504804185116942118292169882845758496562924435108420855027895648573248076211504266851103614057367478195451135747251761655466606998583524320428300740888960588763014904412867710800991415357344150139100691285791044019699484033243496085357550175962781105083367390782828092020739153739871046031928396254645827207169").toByteArray());
			userRepository.save(denis);
			User evgen = new User("eugeneshapovalov94@gmail.com", passwordEncoder.encode("password"), null, 0,new BigInteger("199582142956008510570996053172578267554407915993617735369142167915397142162101697564516553684358371209167917170134601047935528036367371601560005494370611656094063057947755991353242607150997754624052362675573117126612157863475657544266111555006071906212072656208245909853363709450444535316280237576977825698189080456199412848204008955695726106671488788819583661874160117892497024568597189799329021175603907642111985413498084813654261424766963399812182824442159054361173608548037111443447309667283056332575802518098857385811249456332911896652039385543371890567950059449908192816394842006612292940885320931356258775870180884476827854525507663107557287827949866342812617297737182305340840981501001511616578125825").toByteArray());
			userRepository.save(evgen);
			User masha = new User("masha.sokal@gmail.com", passwordEncoder.encode("password"), null, 0,new BigInteger("199582142956008510570996053172578267554407915993617735369142167915397142162101694513805757507183650461186380071749211855681498055328776571653002840300803237350152368014814791047610904501408119685280918491878521300568048323644902703318438526953254965670464740409356943180874517618243236810637334652644577456434513204429493833740815224510395944158717883753044827721901541434713546633351554430051466306106744747985297552193919727454684646970026266828996648837914570410473395578010418048776012279904325789467290873841014770377669469651610006952304521038454745778518211185104506110089448842486983759567324460309590198176112102694130446144948431586819614392249542556852114707417413316505816348874076768168375812097").toByteArray());
			userRepository.save(masha);
			List<UserGroup> userGroups = createGroups();
			List<User> users = userRepository.findAll();
			for(int i = 1; i< userGroups.size() && i<users.size()+1;i++) {
				addUserToGroup(users.get(i-1), userGroups.get(i-1));
				addUserToGroup(users.get(i-1), userGroups.get(i));
			}
		}
	}


	private List<UserGroup> createGroups(){
		if(this.groupRepository.count() < 15){
			List<UserGroup> list = new ArrayList<>();
			String[] groups = new String[]{"Administrators", "Soldiers", "Lance corporals", "Junior sergeants",
					"Sergeants", "Senior sergeants", "Sergeant majors", "Warrant officers", "Junior lieutenants",
					"Lieutenants", "Senior lieutenants", "Captains", "Majors", "Lieutenant colonels",
					"Colonels", "Major-generals", "Lieutenant-generals", "Colonel-generals"};
			for (int i = 0; i < groups.length; i++){

				list.add(new UserGroup(groups[i]));

			}
			this.groupRepository.save(list);
			/*List<FileDescriptor> docList = new ArrayList<>();
			List<Grants> grantList = new ArrayList<>();
			for (int i = 0; i < list.size(); i++){
				UserGroup group = list.get(i);
				for(int j = 0; j<5; j++){
					FileDescriptor doc = new FileDescriptor("doc"+i+j, "doc"+i+j);

					docList.add(doc);
					grantList.add(new Grants(Grants.AccessLevel.READWRITE,group, doc));
				}
			}


			docRepository.save(docList);
			docGroupRepository.save(grantList);*/
			return list;
		}
		return groupRepository.findAll();
	}

	private void addUserToGroup(User user,UserGroup group){
		Set<User> users = group.getUsers()==null?new HashSet<>():group.getUsers();
		users.add(user);
		group.setUsers(users);
		groupRepository.save(group);
	}


}
