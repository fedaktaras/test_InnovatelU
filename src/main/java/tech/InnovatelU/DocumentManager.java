package tech.InnovatelU;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private final Map<String, Document> storage = new HashMap<>();
    private long idPosition = 1;

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */

    public Document save(Document document) {
        if (document.getId() == null) {
            Document documentToStore = copyDocument(document);
            documentToStore.setId(incrementAndGet());

            storage.put(documentToStore.getId(), documentToStore);

            document.setId(documentToStore.getId());
        } else {
            storage.put(document.getId(), copyDocument(document));
        }
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return storage.values().stream()
                .filter(doc -> matches(doc, request))
                .map(this::copyDocument)
                .collect(Collectors.toList());
    }
    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(storage.get(id)).map(this::copyDocument);
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }


    private Document copyDocument(Document document) {
        return Document.builder()
                .id(document.getId())
                .title(document.getTitle())
                .content(document.getContent())
                .author(document.getAuthor() != null ? copyAuthor(document.getAuthor()) : null)
                .created(document.getCreated())
                .build();
    }

    private Author copyAuthor(Author author) {
        return Author.builder()
                .id(author.getId())
                .name(author.getName())
                .build();
    }

    private String incrementAndGet() {
        return String.valueOf(idPosition++);
    }

    private boolean matches(Document doc, SearchRequest req) {
        return matchesTitlePrefixes(doc, req) &&
                matchesContainsContents(doc, req) &&
                matchesAuthorIds(doc, req) &&
                matchesCreatedFrom(doc, req) &&
                matchesCreatedTo(doc, req);
    }

    private boolean matchesTitlePrefixes(Document doc, SearchRequest req) {
        return req.getTitlePrefixes() == null || req.getTitlePrefixes().stream()
                .anyMatch(prefix -> doc.getTitle().startsWith(prefix));
    }

    private boolean matchesContainsContents(Document doc, SearchRequest req) {
        return req.getContainsContents() == null || req.getContainsContents().stream()
                .anyMatch(content -> doc.getContent().contains(content));
    }

    private boolean matchesAuthorIds(Document doc, SearchRequest req) {
        return req.getAuthorIds() == null || req.getAuthorIds().contains(doc.getAuthor().getId());
    }

    private boolean matchesCreatedFrom(Document doc, SearchRequest req) {
        return req.getCreatedFrom() == null || !doc.getCreated().isBefore(req.getCreatedFrom());
    }

    private boolean matchesCreatedTo(Document doc, SearchRequest req) {
        return req.getCreatedTo() == null || !doc.getCreated().isAfter(req.getCreatedTo());
    }
}