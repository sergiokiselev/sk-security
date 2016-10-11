package ch.rasc.sec.secureserver;

import ch.rasc.sec.secureserver.model.DocGroup;
import ch.rasc.sec.secureserver.model.Document;
import ch.rasc.sec.secureserver.model.Group;
import ch.rasc.sec.secureserver.repository.DocGroupRepository;
import ch.rasc.sec.secureserver.repository.DocRepository;
import ch.rasc.sec.secureserver.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InitDatabase implements ApplicationListener<ContextRefreshedEvent> {




    @Autowired
    private DocRepository docRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private DocGroupRepository docGroupRepository;

    @Autowired
    public InitDatabase( DocRepository docRepository) {

    }

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (this.docRepository.count() == 0) {
            Document doc = new Document("doc","doc");


            docRepository.save(doc);

            Group group = new Group("group");
            groupRepository.save(group);
            docGroupRepository.save(new DocGroup(DocGroup.AccessLevel.READWRITE,group,doc));


        }

    }

}
