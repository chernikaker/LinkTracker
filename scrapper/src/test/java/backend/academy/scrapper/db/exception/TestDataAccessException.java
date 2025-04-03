package backend.academy.scrapper.db.exception;

import org.springframework.dao.DataAccessException;

public class TestDataAccessException extends DataAccessException {

    public TestDataAccessException(String msg) {
        super(msg);
    }
}
