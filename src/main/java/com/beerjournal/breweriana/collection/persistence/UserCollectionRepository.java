package com.beerjournal.breweriana.collection.persistence;

import com.beerjournal.breweriana.item.persistence.Item;
import com.beerjournal.infrastructure.error.BeerJournalException;
import com.google.common.base.Strings;
import com.mongodb.WriteResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.beerjournal.infrastructure.error.ErrorInfo.USER_COLLECTION_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class UserCollectionRepository {

    private final UserCollectionCrudRepository crudRepository;
    private final MongoOperations mongoOperations;

    public Optional<UserCollection> findOneByOwnerId(ObjectId ownerId) {
        return crudRepository.findOneByOwnerId(ownerId);
    }

    public Page<ItemRef> findAllInUserCollection(ObjectId ownerId,
                                                 int page,
                                                 int count,
                                                 Map<String, String> filterRequestParams,
                                                 String sortBy,
                                                 String sortType) {
        Criteria criteria = Criteria.where("ownerId").is(ownerId);

        return findAllByParams(criteria, new PageRequest(page, count), filterRequestParams, sortBy, sortType).map(Item::asItemRef);
    }


    public Page<ItemRef> findAllNotInUserCollection(ObjectId ownerId,
                                                    int page,
                                                    int count,
                                                    Map<String, String> filterRequestParams,
                                                    String sortBy,
                                                    String sortType) {
        UserCollection userCollection = crudRepository.findOneByOwnerId(ownerId)
                .orElseThrow(() -> new BeerJournalException(USER_COLLECTION_NOT_FOUND));

        Set<String> userItemsNames = userCollection.getItemRefs().stream()
                .map(ItemRef::getName)
                .collect(Collectors.toSet());

        Criteria criteria = Criteria.where("name").not().in(userItemsNames);

        return findAllByParams(criteria, new PageRequest(page, count), filterRequestParams, sortBy, sortType).map(Item::asItemRef);
    }

    UserCollection deleteOneByOwnerId(ObjectId ownerId) {
        findOneByOwnerId(ownerId)
                .orElseThrow(() -> new BeerJournalException(USER_COLLECTION_NOT_FOUND));

        return mongoOperations.findAndRemove(
                new Query(Criteria.where("ownerId").is(ownerId)),
                UserCollection.class);
    }

    UserCollection save(UserCollection userCollection) {
        return crudRepository.save(userCollection);
    }

    int addNewItem(Item item) {
        ItemRef itemRef = item.asItemRef();

        WriteResult writeResult = mongoOperations.updateFirst(
                new Query(Criteria.where("ownerId").is(item.getOwnerId())),
                new Update().push("itemRefs", itemRef),
                UserCollection.class);

        return writeResult.getN();
    }

    int deleteItem(Item item) {
        ItemRef itemRef = item.asItemRef();

        WriteResult writeResult = mongoOperations.updateFirst(
                new Query(Criteria.where("ownerId").is(item.getOwnerId())),
                new Update().pull("itemRefs", itemRef),
                UserCollection.class);

        return writeResult.getN();
    }

    int updateItem(Item item) {
        WriteResult writeResult = mongoOperations.updateFirst(
                new Query(new Criteria().andOperator(
                        Criteria.where("ownerId").is(item.getOwnerId()),
                        Criteria.where("itemRefs").elemMatch(Criteria.where("itemId").is(item.getId()))
                )),
                new Update()
                        .set("itemRefs.$.name", item.getName())
                        .set("itemRefs.$.type", item.getType())
                        .set("itemRefs.$.imageId", item.getMainImageId()),
                UserCollection.class);

        return writeResult.getN();
    }

    private Page<Item> findAllByParams(Criteria criteria,
                                       PageRequest pageRequest,
                                       Map<String, String> allRequestParams,
                                       String sortBy,
                                       String sortType) {
        List<Criteria> filterCriterias = new ArrayList<>();

        for (Map.Entry<String, String> entry : allRequestParams.entrySet()) {
            if (entry.getKey().matches("name|type|country|brewery|style|averagerating") && !Strings.isNullOrEmpty(entry.getValue())) {
                if (entry.getKey().equals("averagerating")) {
                    if (NumberUtils.isParsable(entry.getValue()))
                        filterCriterias.add(Criteria.where("averageRating").is(Double.valueOf(entry.getValue())));
                } else {
                    filterCriterias.add(Criteria.where(entry.getKey()).regex(Pattern.compile("^" + entry.getValue())));

                }
            }
        }

        if (!filterCriterias.isEmpty())
            criteria.andOperator(filterCriterias.toArray(new Criteria[filterCriterias.size()]));

        Query query = new Query(criteria);
        long total = mongoOperations.count(query, Item.class);
        query.with(pageRequest);

        if (sortBy.matches("name|type|country|brewery|style|createtime|averagerating")) {
            query.with(new Sort(new Sort.Order(
                    sortType.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                    sortBy.equals("createtime") ? "_id" : sortBy.equals("averagerating") ? "averageRating" : sortBy)));
        }

        List<Item> items = mongoOperations.find(query, Item.class);

        return new PageImpl<>(items, pageRequest, total);
    }
}
