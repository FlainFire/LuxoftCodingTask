package by.okovity.task.repo;

import by.okovity.task.entity.Record;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecordRepo extends CrudRepository<Record, Long> {
}
