package com.beerjournal.breweriana.persistence

import com.beerjournal.breweriana.persistence.category.Category
import com.beerjournal.breweriana.persistence.collection.UserCollection
import com.beerjournal.breweriana.utils.TestUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@SpringBootTest
@ActiveProfiles("test")
class ItemRepositoryTest extends Specification {

    @Autowired
    ItemRepository itemRepository

    @Autowired
    ItemCrudRepository itemCrudRepository

    @Autowired
    CategoryCrudRepository categoryRepository

    @Autowired
    UserCollectionCrudRepository userCollectionCrudRepository

    @Autowired
    UserCollectionRepository userCollectionRepository

    @Autowired
    UserCrudRepository userRepository

    @Autowired
    MongoTemplate mongoTemplate

    def savedUser
    def someUserCollection

    def setup() {
        savedUser = userRepository.save(TestUtils.someUser())
        someUserCollection = UserCollection.builder()
                .ownerId(savedUser.id)
                .build()
        mongoTemplate.db.dropDatabase()
    }

    def someItem1 = TestUtils.someItem()
    def someItem2 = TestUtils.someItem()

    def "should save an item"() {
        when:
        itemRepository.save(someItem1)

        then:
        def maybeItem = itemCrudRepository.findOneByName(someItem1.name)
        TestUtils.equalsOptionalValue(maybeItem, someItem1)
    }

    def "should add category if not exists"() {
        when:
        itemRepository.save(someItem1)

        then:
        def maybeCategory = categoryRepository.findOneByName(someItem1.category)
        TestUtils.equalsOptionalValue(maybeCategory, Category.of(someItem1.category))
    }

    def "should return not owned items"() {
        given:
        userCollectionCrudRepository.save(someUserCollection)
        itemRepository.save(someItem1)
        itemRepository.save(someItem2)

        when:
        def missingItems = itemRepository.findAllNotInUserCollection(savedUser.id);

        then:
        missingItems.containsAll(Arrays.asList(someItem1, someItem2))
    }

    def "shouldn't return owned item"() {
        given:
        userCollectionCrudRepository.save(someUserCollection)
        itemRepository.save(someItem1)
        userCollectionRepository.addNewItem(savedUser.id, someItem1)

        when:
        def missingItems = itemRepository.findAllNotInUserCollection(savedUser.id);

        then:
        !missingItems.contains(someItem1)
    }

}