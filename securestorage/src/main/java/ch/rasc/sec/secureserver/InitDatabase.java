package ch.rasc.sec.secureserver;

import ch.rasc.sec.secureserver.model.Document;
import ch.rasc.sec.secureserver.repository.DocRepository;
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

    }

}
