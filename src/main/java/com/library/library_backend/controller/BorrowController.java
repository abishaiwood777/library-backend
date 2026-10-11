package com.library.library_backend.controller;

import com.library.library_backend.model.*;
import com.library.library_backend.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/borrow")
@CrossOrigin
public class BorrowController {

    private final BorrowRecordRepository recordRepo;
    private final BookRepository bookRepo;
    private final UserRepository userRepo;

    public BorrowController(BorrowRecordRepository recordRepo, BookRepository bookRepo, UserRepository userRepo) {
        this.recordRepo = recordRepo;
        this.bookRepo = bookRepo;
        this.userRepo = userRepo;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> borrow(@RequestParam Long userId, @RequestParam Long bookId) {
        User user = userRepo.findById(userId).orElse(null);
        Book book = bookRepo.findById(bookId).orElse(null);
        if (user == null || book == null) {
            return ResponseEntity.badRequest().body("User or book not found");
        }
        if (book.getAvailableCopies() <= 0) {
            return ResponseEntity.badRequest().body("No copies available");
        }
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepo.save(book);

        BorrowRecord record = new BorrowRecord();
        record.setUser(user);
        record.setBook(book);
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(14));
        return ResponseEntity.ok(recordRepo.save(record));
    }

    @PutMapping("/{recordId}/return")
    @Transactional
    public ResponseEntity<?> returnBook(@PathVariable Long recordId) {
        BorrowRecord record = recordRepo.findById(recordId).orElse(null);
        if (record == null) {
            return ResponseEntity.badRequest().body("Record not found");
        }
        if (record.getReturnDate() != null) {
            return ResponseEntity.badRequest().body("Book already returned");
        }
        record.setReturnDate(LocalDate.now());
        Book book = record.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepo.save(book);
        return ResponseEntity.ok(recordRepo.save(record));
    }

    @GetMapping("/user/{userId}")
    public List<BorrowRecord> history(@PathVariable Long userId) {
        return recordRepo.findByUserId(userId);
    }

    @GetMapping
    public List<BorrowRecord> allRecords() {
        return recordRepo.findAll();
    }
}