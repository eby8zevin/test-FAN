import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_database/firebase_database.dart';
import 'package:flutter/material.dart';

// void main() {
//   runApp(const MyApp());
// }

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key});

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  final FirebaseAuth _auth = FirebaseAuth.instance;
  // ignore: deprecated_member_use
  final DatabaseReference _database = FirebaseDatabase(
    databaseURL:
        'https://fanintek-bekasi-default-rtdb.asia-southeast1.firebasedatabase.app',
    // ignore: deprecated_member_use
  ).reference().child('users');

  Future<void> sendConfirmationEmail(BuildContext context) async {
    User? user = _auth.currentUser;

    if (user == null) {
      showDialog(
        context: context,
        builder: (BuildContext context) {
          return AlertDialog(
            title: const Text('Error'),
            content: const Text('User not found.'),
            actions: [
              ElevatedButton(
                child: const Text('OK'),
                onPressed: () {
                  Navigator.of(context).pop();
                },
              ),
            ],
          );
        },
      );
      return;
    }

    await user.sendEmailVerification();

    // ignore: use_build_context_synchronously
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text('Success'),
          content:
              const Text('Confirmation email sent. Please check your email.'),
          actions: [
            ElevatedButton(
              child: const Text('OK'),
              onPressed: () {
                Navigator.of(context).pop();
              },
            ),
          ],
        );
      },
    );
  }

  Future<List<Map<String, dynamic>>> getUserData(BuildContext context) async {
    User? user = _auth.currentUser;

    if (user == null) {
      showDialog(
        context: context,
        builder: (BuildContext context) {
          return AlertDialog(
            title: const Text('Error'),
            content: const Text('User not found.'),
            actions: [
              ElevatedButton(
                child: const Text('OK'),
                onPressed: () {
                  Navigator.of(context).pop();
                },
              ),
            ],
          );
        },
      );
      return [];
    }

    DataSnapshot snapshot = (await _database
        .child('users')
        .orderByChild('confirmed')
        .equalTo(true)
        .once()) as DataSnapshot;

    List<Map<String, dynamic>> userData = [];

    if (snapshot.value != null) {
      Map<dynamic, dynamic>? data = snapshot.value as Map<dynamic, dynamic>?;
      if (data != null) {
        data.forEach((key, value) {
          userData.add(Map<String, dynamic>.from(value));
        });
      }
    }

    return userData;
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
        child: Scaffold(
      appBar: AppBar(
        title: const Text('Home'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            ElevatedButton(
              child: const Text('Send Confirmation Email'),
              onPressed: () {
                sendConfirmationEmail(context);
              },
            ),
            const SizedBox(height: 16.0),
            FutureBuilder<List<Map<String, dynamic>>>(
              future: getUserData(context),
              builder: (context, snapshot) {
                if (snapshot.connectionState == ConnectionState.waiting) {
                  return const CircularProgressIndicator();
                } else if (snapshot.hasError) {
                  return Text('Error: ${snapshot.error}');
                } else if (snapshot.hasData) {
                  List<Map<String, dynamic>> userData = snapshot.data!;
                  return Column(
                    children: userData.map((data) {
                      return ListTile(
                        title: Text(data['name']),
                        subtitle: Text(data['email']),
                      );
                    }).toList(),
                  );
                } else {
                  return const Text('No data available.');
                }
              },
            ),
          ],
        ),
      ),
    ));
  }
}
