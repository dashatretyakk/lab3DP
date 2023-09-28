package main

import (
	"context"
	"fmt"
	"golang.org/x/sync/semaphore"
	"math/rand"
	"sync"
	"time"
)

type Table struct {
	tobacco  bool
	paper    bool
	match    bool
	mu       sync.Mutex
	semAgent *semaphore.Weighted
}

// Reset скидає стан столу до початкового (жоден із компонентів не присутній).
func (t *Table) Reset() {
	t.tobacco = false
	t.paper = false
	t.match = false
}

// NewTable створює новий екземпляр столу з семафором для посередника.
func NewTable() *Table {
	return &Table{
		semAgent: semaphore.NewWeighted(1),
	}
}

// smoker моделює процес куріння курця, який має один з компонентів.
func smoker(t *Table, component string, sleepTime time.Duration) {
	for {
		t.mu.Lock()
		// Залежно від компоненту, який має курець, він перевіряє, чи є на столі решта компонентів для сигарети.
		switch component {
		case "tobacco":
			if t.paper && t.match {
				t.Reset()
				t.mu.Unlock()
				fmt.Println("Курець з тютюном скручує сигарету і курить.")
				time.Sleep(sleepTime)
				t.semAgent.Release(1)
				continue
			}
		case "paper":
			if t.tobacco && t.match {
				t.Reset()
				t.mu.Unlock()
				fmt.Println("Курець з папером скручує сигарету і курить.")
				time.Sleep(sleepTime)
				t.semAgent.Release(1)
				continue
			}
		case "match":
			if t.tobacco && t.paper {
				t.Reset()
				t.mu.Unlock()
				fmt.Println("Курець з сірниками скручує сигарету і курить.")
				time.Sleep(sleepTime)
				t.semAgent.Release(1)
				continue
			}
		}
		t.mu.Unlock()
		time.Sleep(50 * time.Millisecond)
	}
}

// agent моделює роботу посередника, який кладе на стіл два компоненти з трьох.
func agent(t *Table) {
	for {
		err := t.semAgent.Acquire(context.TODO(), 1)
		if err != nil {
			return
		}
		choice := rand.Intn(3)
		t.mu.Lock()
		switch choice {
		case 0:
			t.tobacco = true
			t.paper = true
			fmt.Println("Посередник кладе тютюн та папір на стіл.")
		case 1:
			t.tobacco = true
			t.match = true
			fmt.Println("Посередник кладе тютюн та сірники на стіл.")
		case 2:
			t.paper = true
			t.match = true
			fmt.Println("Посередник кладе папір та сірники на стіл.")
		}
		t.mu.Unlock()
	}
}

func main() {
	table := NewTable()

	// Запуск потоків для трьох курців.
	go smoker(table, "tobacco", 1*time.Second)
	go smoker(table, "paper", 1*time.Second)
	go smoker(table, "match", 1*time.Second)

	// Запуск потоку для посередника.
	agent(table)
}
