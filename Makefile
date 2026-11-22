.PHONY: dev test migration diagramas prints

dev:
	docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build

test:
	mvn test

migration:
	@test -n "$(nome)" || { echo "Uso: make migration nome=marca_site"; exit 1; }
	mvn -q test -Dtest=GerarMigration -Dmigration=$(nome)

diagramas:
	docker run --rm -u $$(id -u):$$(id -g) -v $(CURDIR)/docs:/data plantuml/plantuml -tpng '/data/*.puml' '/data/sequencia/*.puml'
	docker run --rm -u $$(id -u):$$(id -g) -e HOME=/tmp -v $(CURDIR)/docs:/data rlespinasse/drawio-desktop-headless -x -f pdf --crop -o /data/ /data/
	for f in docs/*.mmd; do docker run --rm -u $$(id -u):$$(id -g) -v $(CURDIR)/docs:/data minlag/mermaid-cli -i /data/$$(basename $$f) -o /data/$$(basename $$f .mmd).pdf; done

prints:
	docs/prints/prints.sh
