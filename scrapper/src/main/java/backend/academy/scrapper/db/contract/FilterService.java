package backend.academy.scrapper.db.contract;

import backend.academy.scrapper.entity.Filter;
import java.util.List;

/** Контракт сервиса, управляющего фильтрами в БД */
public interface FilterService {

    /** Получение списка фильтров по id подписки */
    List<Filter> getFiltersBySubscriptionId(long id);
}
