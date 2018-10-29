package com.karmazin;

import com.karmazin.model.LoggerAPI;
import com.karmazin.model.YandexMapsAPI;
import javafx.scene.Scene;

import javax.swing.*;

public class FokenTrash {
    private static LoggerAPI logger = new LoggerAPI(FokenTrash.class.getName());

    private static JFrame frame;

    // Login screen FokenTrash-objects
    private static String loginXml;
    private static Scene loginScene;

    private static final YandexMapsAPI yandexMapsAPI = new YandexMapsAPI(400, 400);
    private static final int delay = 100;

    /*
    public static void display() {
        //Creating the Frame
        frame = new JFrame("Application");

        Pair<Integer, Integer> resolution = ConfigAPI.getResolution();

        frame.setSize(resolution.getValue(), resolution.getKey());
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitProcedure();
            }
        });

        //Creating the MenuBar and adding components
        // -------------------------------------- MENU --------------------------------------
        JMenuBar mb = new JMenuBar();

        JMenu m1 = new JMenu("Файл");
        mb.add(m1);
        JMenu m2 = new JMenu("Помощь");
        mb.add(m2);

        JMenuItem m11 = new JMenuItem("Сохранить");
        m11.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Сохраняю");
            }
        });
        m1.add(m11);

        JMenuItem m22 = new JMenuItem("Загрузить");
        m22.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Загружаю");
            }
        });
        m1.add(m22);

        // -------------------------------------- JButton --------------------------------------

        // -------------------------------------- JTabbedPane --------------------------------------
        final JTabbedPane tp = new JTabbedPane();
        tp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tp.addTab("Начальная вкладка", makeTextPanel("Начальная вкладка, стоит ограничение на 1 сервер!"));

        //Creating the panel at bottom and adding components
        JPanel panel = new JPanel();

        JLabel label = new JLabel("IP/HTTP");
        panel.add(label);

        final JTextField tf = new JTextField(10); // accepts up to 10 characters
        panel.add(tf);

        // -------------------------------------- JButton --------------------------------------
        JButton addServer = new JButton("Добавить сервер");
        addServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!tf.getText().isEmpty() && tp.getTabCount() < 2) {
                    // --------------------------------------- MultiThread ----------------------------
                    if (ConfigAPI.addServerTab(tf.getText(), delay)) {
                        // Юзер имеет право на выполнение комманды
                        tp.addTab(tf.getText(), contentPanel(tf.getText()));
                        tp.setSelectedIndex(tp.getTabCount() - 1);
                        tf.setText("");

                        System.out.println("Сервер " + tf.getText() + " был добавлен!");
                    } else {
                        // Нет прав на выполнение комманды
                        System.out.println("Пользователь не авторизован!");
                    }
                } else {
                    System.err.println("Введите имя сервера!");
                }
            }
        });
        panel.add(addServer);
        frame.getRootPane().setDefaultButton(addServer);

        JButton removeServer = new JButton("Удалить сервер");
        removeServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tp.getTabCount() > 1) {
                    if (ConfigAPI.closeServerTab()) {
                        // UserAPI have authority for operation
                        tp.removeTabAt(tp.getSelectedIndex());

                        System.out.println("Сервер " + tp.getTitleAt(tp.getSelectedIndex()) + " был удалён!");
                    } else {
                        // UserAPI doesn't have authority for operation
                        System.out.println("Пользователь не авторизован!");
                    }
                } else {
                    System.err.println("Переход на начальную вкладку");
                    tp.addTab("Начальная вкладка", makeTextPanel("Начальная вкладка, стоит ограничение на 1 сервер!"));
                    tp.removeTabAt(0);
                }
            }
        });
        panel.add(removeServer);

        JButton unloginButton = new JButton("Выйти из аккаунта");
        unloginButton.addActionListener(event -> {
            ConfigAPI.setResolution(frame.getHeight(), frame.getWidth());
            frame.dispose();
            ConfigAPI.unlogin();
        });
        panel.add(unloginButton);

        if (ConfigAPI.sessionStatus().equals(UserAPI.UserType.Developer)) {
            JButton debugButton = new JButton("Отладка");
            debugButton.addActionListener(event -> ConfigAPI.setDebug(true));
            panel.add(debugButton);
            JButton selfTestButton = new JButton("Тестирование");
            selfTestButton.addActionListener(event -> ConfigAPI.setSelfTest(true));
            panel.add(selfTestButton);
        }

        // -------------------------------------- Adding Components --------------------------------------
        frame.getContentPane().add(BorderLayout.SOUTH, panel);
        frame.getContentPane().add(BorderLayout.NORTH, mb);
        frame.getContentPane().add(BorderLayout.CENTER, tp);
        frame.setVisible(true);
        tf.setText("vk.com");
        tf.requestFocusInWindow();
    }


    private static JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        return panel;
    }

    // -------------------------------------- ContentPanel --------------------------------------
    private static JComponent contentPanel(String IP) {
        GeolocationAPIData geoData = ConfigAPI.getGeoData(IP);
        JPanel contentPanel = new JPanel();

        if (geoData != null) {
            // UserAPI have authority for operation
            contentPanel.setBackground(Color.WHITE);

            JPanel infoPanel = new JPanel();
            infoPanel.setBackground(Color.WHITE);
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.X_AXIS));


            JLabel graphLabel = new JLabel("<html>" + geoData.toString() + "</html>");
            graphLabel.setPreferredSize(new Dimension(400, 200));

            infoPanel.add(graphLabel);
            //infoPanel.add(new JLabel(yandexMapsAPI.getMapImageIcon(geoData.getLat(), geoData.getLon())));

            //contentPanel.add(createPane(delay));
            contentPanel.add(infoPanel);
        }

        logger.log(Level.INFO, "Generating contentPanel");
        return contentPanel;
    }
    */


}

