package ch.rasc.sec;

import ch.rasc.sec.cypher.AES;
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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}

		if (this.userRepository.count() == 0) {
			User adminUser = new User();
			adminUser.setEmail("superadmin@gmail.com");
			adminUser.setSecret("IB6EFEQKE7U2TQIB");
			adminUser.setTries(0);
			adminUser.setPassword(passwordEncoder.encode("password"));
			userRepository.save(adminUser);
			User seorgy = new User("sergio.kiselev509@gmail.com", passwordEncoder.encode("password"), null, 0);
			userRepository.save(seorgy);
			User denis = new User("m-den-i@yandex.by", passwordEncoder.encode("password"), null, 0);
			userRepository.save(denis);
			User evgen = new User("eugeneshapovalov94@gmail.com", passwordEncoder.encode("password"), null, 0);
			userRepository.save(evgen);
			User masha = new User("masha.sokal@gmail.com", passwordEncoder.encode("password"), null, 0);
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
