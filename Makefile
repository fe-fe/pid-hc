.PHONY: dev test migration

dev:
	docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build

test:
	mvn test

migration:
	@test -n "$(nome)" || { echo "Uso: make migration nome=marca_site"; exit 1; }
	mvn -q test -Dtest=GerarMigration -Dmigration=$(nome)
