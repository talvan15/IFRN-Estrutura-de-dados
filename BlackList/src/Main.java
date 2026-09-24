package BlackList.src;

public class Main {

    public static void main(String[] args) {

        BlackListBST blacklist = new BlackListBST();

        // INSERINDO IPs

        blacklist.insert(192168001001L);
        blacklist.insert(172160001010L);
        blacklist.insert(200100050001L);
        blacklist.insert(100000000001L);
        blacklist.insert(180200030001L);
        blacklist.insert(220100010001L);


        // IP repetido
        blacklist.insert(192168001001L);
        blacklist.insert(192168001001L);


        // BUSCANDO IPs

        System.out.println("===== BUSCA =====");

        blacklist.search(192168001001L);

        System.out.println();

        blacklist.search(123456789L);


        // RELATÓRIO

        blacklist.relatorioOrdenado();


        // REMOÇÃO

        System.out.println("\n===== REMOÇÃO =====");

        System.out.println("Removendo IP 172160001010...");

        blacklist.remover(172160001010L);


        // NOVO RELATÓRIO

        blacklist.relatorioOrdenado();
    }
}