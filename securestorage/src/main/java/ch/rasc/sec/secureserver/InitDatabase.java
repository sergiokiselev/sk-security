package ch.rasc.sec.secureserver;

import ch.rasc.sec.secureserver.model.Document;
import ch.rasc.sec.secureserver.model.Group;
import ch.rasc.sec.secureserver.model.User;
import ch.rasc.sec.secureserver.repository.DocRepository;
import ch.rasc.sec.secureserver.repository.UserRepository;
import ch.rasc.sec.secureserver.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Component
public class InitDatabase implements ApplicationListener<ContextRefreshedEvent> {




    @Autowired
    private DocRepository docRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    public InitDatabase( DocRepository docRepository) {

    }

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (this.docRepository.count() == 0) {
            Document doc = new Document("doc","doc");

            //doc.setName("doc");
            //doc.setLink("doc");
            docRepository.save(doc);

        }
        if (this.userRepository.count() == 0) {
            User user = new User("userLogin","user name", "user@mail.com");
            userRepository.save(user);
        }
        if(this.groupRepository.count() == 0){
            List<Group> list = new ArrayList<>();
            String[] groups = new String[]{"Administrators", "Soldiers", "Lance corporals", "Junior sergeants",
                    "Sergeants", "Senior sergeants", "Sergeant majors", "Warrant officers", "Junior lieutenants",
                    "Lieutenants", "Senior lieutenants", "Captains", "Majors", "Lieutenant colonels",
                    "Colonels", "Major-generals", "Lieutenant-generals", "Colonel-generals"};
            for (String string: groups){
                list.add(new Group(string));
            }
            groupRepository.save(list);
        }
    }

}
